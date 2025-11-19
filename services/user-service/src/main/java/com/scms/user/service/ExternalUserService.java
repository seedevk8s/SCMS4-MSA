package com.scms.user.service;

import com.scms.common.exception.ApiException;
import com.scms.common.exception.ErrorCode;
import com.scms.user.domain.entity.ExternalUser;
import com.scms.user.domain.enums.AccountStatus;
import com.scms.user.dto.request.ExternalUserCreateRequest;
import com.scms.user.dto.response.UserResponse;
import com.scms.user.repository.ExternalUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 외부 사용자 관리 서비스
 *
 * 주요 기능:
 * - 외부 사용자 회원가입 (로컬)
 * - OAuth2 소셜 로그인 연동
 * - 이메일 인증
 * - 계정 상태 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExternalUserService {

    private final ExternalUserRepository externalUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * 외부 사용자 회원가입 (로컬)
     */
    @Transactional
    public UserResponse registerExternalUser(ExternalUserCreateRequest request) {
        // 1. 이메일 중복 체크
        if (externalUserRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(ErrorCode.DUPLICATE_EMAIL,
                    "이미 사용 중인 이메일입니다: " + request.getEmail());
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. 이메일 인증 코드 생성
        String verificationCode = UUID.randomUUID().toString();

        // 4. ExternalUser 엔티티 생성
        ExternalUser externalUser = ExternalUser.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .phone(request.getPhone())
                .organization(request.getOrganization())
                .position(request.getPosition())
                .provider(null)  // 로컬 가입
                .providerId(null)
                .emailVerified(false)
                .emailVerificationCode(verificationCode)
                .accountStatus(AccountStatus.INACTIVE)  // 이메일 인증 전까지 비활성
                .termsAgreed(request.getTermsAgreed())
                .privacyAgreed(request.getPrivacyAgreed())
                .marketingAgreed(request.getMarketingAgreed())
                .build();

        // 5. 저장
        ExternalUser savedUser = externalUserRepository.save(externalUser);
        log.info("외부 사용자 회원가입 완료: externalUserId={}, email={}",
                savedUser.getExternalUserId(), savedUser.getEmail());

        // 6. 이메일 인증 코드 발송
        emailService.sendVerificationEmail(savedUser.getEmail(), verificationCode);

        return UserResponse.fromExternal(savedUser);
    }

    /**
     * OAuth2 소셜 로그인 사용자 생성/업데이트
     */
    @Transactional
    public ExternalUser processOAuthUser(
            String provider,
            String providerId,
            String email,
            String name
    ) {
        // 1. 기존 사용자 확인 (provider + providerId)
        return externalUserRepository.findByProviderAndProviderIdAndDeletedAtIsNull(provider, providerId)
                .map(user -> {
                    // 기존 사용자 업데이트
                    user.setName(name);
                    user.setLastLoginAt(LocalDateTime.now());
                    ExternalUser updatedUser = externalUserRepository.save(user);
                    log.info("OAuth 사용자 정보 업데이트: provider={}, providerId={}", provider, providerId);
                    return updatedUser;
                })
                .orElseGet(() -> {
                    // 신규 사용자 생성
                    ExternalUser newUser = ExternalUser.builder()
                            .email(email)
                            .name(name)
                            .provider(provider)
                            .providerId(providerId)
                            .emailVerified(true)  // OAuth는 이메일 인증 완료로 간주
                            .accountStatus(AccountStatus.ACTIVE)
                            .termsAgreed(true)
                            .privacyAgreed(true)
                            .marketingAgreed(false)
                            .lastLoginAt(LocalDateTime.now())
                            .build();

                    ExternalUser savedUser = externalUserRepository.save(newUser);
                    log.info("OAuth 신규 사용자 생성: provider={}, providerId={}, email={}",
                            provider, providerId, email);

                    // 환영 이메일 발송
                    emailService.sendWelcomeEmail(email, name);

                    return savedUser;
                });
    }

    /**
     * 이메일 인증
     */
    @Transactional
    public void verifyEmail(String verificationCode) {
        ExternalUser user = externalUserRepository
                .findByEmailVerificationCodeAndEmailVerifiedFalse(verificationCode)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_VERIFICATION_CODE,
                        "유효하지 않은 인증 코드입니다."));

        user.setEmailVerified(true);
        user.setEmailVerificationCode(null);
        user.setAccountStatus(AccountStatus.ACTIVE);  // 인증 후 활성화

        externalUserRepository.save(user);
        log.info("이메일 인증 완료: externalUserId={}, email={}", user.getExternalUserId(), user.getEmail());

        // 환영 이메일 발송
        emailService.sendWelcomeEmail(user.getEmail(), user.getName());
    }

    /**
     * 이메일 인증 코드 재발송
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        ExternalUser user = externalUserRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (user.getEmailVerified()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "이미 인증된 이메일입니다.");
        }

        // 새 인증 코드 생성
        String newCode = UUID.randomUUID().toString();
        user.setEmailVerificationCode(newCode);
        externalUserRepository.save(user);

        // 이메일 발송
        emailService.sendVerificationEmail(email, newCode);
        log.info("이메일 인증 코드 재발송: email={}", email);
    }

    /**
     * 외부 사용자 조회 (ID)
     */
    public ExternalUser getExternalUserById(Long externalUserId) {
        return externalUserRepository.findById(externalUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 외부 사용자 조회 (이메일)
     */
    public ExternalUser getExternalUserByEmail(String email) {
        return externalUserRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 전체 외부 사용자 조회
     */
    public List<UserResponse> getAllExternalUsers() {
        return externalUserRepository.findAllActive()
                .stream()
                .map(UserResponse::fromExternal)
                .collect(Collectors.toList());
    }

    /**
     * OAuth Provider별 사용자 조회
     */
    public List<ExternalUser> getUsersByProvider(String provider) {
        return externalUserRepository.findByProviderAndDeletedAtIsNull(provider);
    }

    /**
     * 미인증 사용자 조회
     */
    public List<ExternalUser> getUnverifiedUsers() {
        return externalUserRepository.findByEmailVerifiedFalseAndDeletedAtIsNull();
    }

    /**
     * 계정 상태 변경
     */
    @Transactional
    public void updateAccountStatus(Long externalUserId, AccountStatus status) {
        ExternalUser user = getExternalUserById(externalUserId);
        user.setAccountStatus(status);
        externalUserRepository.save(user);
        log.info("외부 사용자 계정 상태 변경: externalUserId={}, status={}", externalUserId, status);
    }

    /**
     * 계정 정지
     */
    @Transactional
    public void suspendAccount(Long externalUserId) {
        updateAccountStatus(externalUserId, AccountStatus.SUSPENDED);
    }

    /**
     * 계정 활성화
     */
    @Transactional
    public void activateAccount(Long externalUserId) {
        updateAccountStatus(externalUserId, AccountStatus.ACTIVE);
    }

    /**
     * 외부 사용자 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteExternalUser(Long externalUserId) {
        ExternalUser user = getExternalUserById(externalUserId);
        user.delete();
        externalUserRepository.save(user);
        log.info("외부 사용자 삭제 완료: externalUserId={}", externalUserId);
    }

    /**
     * Provider별 사용자 수 조회
     */
    public long countByProvider(String provider) {
        return externalUserRepository.countByProviderAndDeletedAtIsNull(provider);
    }

    /**
     * 전체 활성 사용자 수
     */
    public long countActiveUsers() {
        return externalUserRepository.countByAccountStatusAndDeletedAtIsNull(AccountStatus.ACTIVE);
    }
}

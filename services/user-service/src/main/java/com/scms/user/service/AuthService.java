package com.scms.user.service;

import com.scms.common.exception.ApiException;
import com.scms.common.exception.ErrorCode;
import com.scms.user.domain.entity.*;
import com.scms.user.domain.enums.TokenType;
import com.scms.user.dto.request.*;
import com.scms.user.dto.response.LoginResponse;
import com.scms.user.dto.response.UserResponse;
import com.scms.user.repository.*;
import com.scms.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 인증 서비스
 *
 * 주요 기능:
 * - 로그인/로그아웃
 * - 비밀번호 변경/재설정
 * - JWT 토큰 발급 및 검증
 * - 로그인 히스토리 기록
 * - 계정 잠금 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final ExternalUserRepository externalUserRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * 일반 사용자 로그인 (내부 사용자)
     */
    @Transactional
    public LoginResponse loginInternal(LoginRequest request, String ipAddress, String userAgent) {
        // 1. 사용자 조회 (학번 또는 이메일)
        User user = userRepository.findByStudentNumAndDeletedAtIsNull(request.getLoginId())
                .or(() -> userRepository.findByEmailAndDeletedAtIsNull(request.getLoginId()))
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 2. 계정 상태 확인
        validateUserAccount(user);

        // 3. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleLoginFailure(user, ipAddress, userAgent);
            throw new ApiException(ErrorCode.INVALID_PASSWORD, "비밀번호가 일치하지 않습니다.");
        }

        // 4. 로그인 성공 처리
        handleLoginSuccess(user, ipAddress, userAgent);

        // 5. JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        // 6. 응답 생성
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenValidityInSeconds())
                .userType("INTERNAL")
                .user(UserResponse.from(user))
                .build();
    }

    /**
     * 외부 사용자 로그인
     */
    @Transactional
    public LoginResponse loginExternal(LoginRequest request, String ipAddress, String userAgent) {
        // 1. 외부 사용자 조회
        ExternalUser externalUser = externalUserRepository.findByEmailAndDeletedAtIsNull(request.getLoginId())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 2. 계정 상태 확인
        validateExternalUserAccount(externalUser);

        // 3. 비밀번호 검증 (로컬 가입자만)
        if (externalUser.getProvider() == null) {  // 로컬 가입
            if (!passwordEncoder.matches(request.getPassword(), externalUser.getPassword())) {
                handleExternalLoginFailure(externalUser, ipAddress, userAgent);
                throw new ApiException(ErrorCode.INVALID_PASSWORD, "비밀번호가 일치하지 않습니다.");
            }
        }

        // 4. 로그인 성공 처리
        handleExternalLoginSuccess(externalUser, ipAddress, userAgent);

        // 5. JWT 토큰 생성 (ExternalUser용 별도 토큰)
        String accessToken = createExternalUserToken(externalUser);
        String refreshToken = createExternalUserRefreshToken(externalUser);

        // 6. 응답 생성
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenValidityInSeconds())
                .userType("EXTERNAL")
                .user(UserResponse.fromExternal(externalUser))
                .build();
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 2. 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ApiException(ErrorCode.INVALID_PASSWORD, "현재 비밀번호가 일치하지 않습니다.");
        }

        // 3. 새 비밀번호 암호화 및 저장
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);
        user.setPasswordUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        log.info("비밀번호 변경 완료: userId={}", userId);
    }

    /**
     * 비밀번호 재설정 요청 (이메일 발송)
     */
    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) {
        // 1. 사용자 조회 (이메일로)
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElse(null);

        ExternalUser externalUser = null;
        if (user == null) {
            externalUser = externalUserRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                    .orElse(null);
        }

        // 2. 사용자가 없어도 보안상 동일한 응답 (타이밍 공격 방지)
        if (user == null && externalUser == null) {
            log.warn("비밀번호 재설정 요청 - 존재하지 않는 이메일: {}", request.getEmail());
            return;  // 에러를 던지지 않음
        }

        // 3. 기존 토큰 만료 처리
        passwordResetTokenRepository.deleteByEmailAndUsedFalse(request.getEmail());

        // 4. 새 토큰 생성
        PasswordResetToken token;
        if (user != null) {
            token = PasswordResetToken.createForUser(user);
        } else {
            token = PasswordResetToken.createForExternalUser(externalUser);
        }
        passwordResetTokenRepository.save(token);

        // 5. 이메일 발송
        emailService.sendPasswordResetEmail(request.getEmail(), token.getToken());
        log.info("비밀번호 재설정 이메일 발송 완료: {}", request.getEmail());
    }

    /**
     * 비밀번호 재설정 (토큰 사용)
     */
    @Transactional
    public void resetPassword(String token, PasswordResetConfirmRequest request) {
        // 1. 토큰 조회 및 검증
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN, "유효하지 않은 토큰입니다."));

        if (!resetToken.isValid()) {
            throw new ApiException(ErrorCode.EXPIRED_TOKEN, "만료된 토큰입니다.");
        }

        // 2. 새 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());

        // 3. 사용자 타입에 따라 비밀번호 업데이트
        if (resetToken.getTokenType() == TokenType.INTERNAL && resetToken.getUser() != null) {
            User user = resetToken.getUser();
            user.setPassword(encodedPassword);
            user.setPasswordUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        } else if (resetToken.getTokenType() == TokenType.EXTERNAL && resetToken.getExternalUser() != null) {
            ExternalUser externalUser = resetToken.getExternalUser();
            externalUser.setPassword(encodedPassword);
            externalUserRepository.save(externalUser);
        }

        // 4. 토큰 사용 처리
        resetToken.use();
        passwordResetTokenRepository.save(resetToken);

        log.info("비밀번호 재설정 완료: email={}", resetToken.getEmail());
    }

    /**
     * JWT 토큰 검증
     */
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    /**
     * 토큰 갱신 (Refresh Token 사용)
     */
    @Transactional
    public LoginResponse refreshToken(String refreshToken) {
        // 1. 토큰 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new ApiException(ErrorCode.INVALID_TOKEN, "유효하지 않은 Refresh Token입니다.");
        }

        // 2. 사용자 정보 추출
        String studentNum = jwtTokenProvider.getStudentNum(refreshToken);

        // 3. 사용자 조회
        User user = userRepository.findByStudentNumAndDeletedAtIsNull(studentNum)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 4. 새 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(user);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenValidityInSeconds())
                .userType("INTERNAL")
                .user(UserResponse.from(user))
                .build();
    }

    // ==================== Private Helper Methods ====================

    /**
     * 내부 사용자 계정 상태 검증
     */
    private void validateUserAccount(User user) {
        if (user.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.USER_DELETED, "삭제된 계정입니다.");
        }
        if (user.getLocked()) {
            throw new ApiException(ErrorCode.ACCOUNT_LOCKED,
                    "계정이 잠겼습니다. 관리자에게 문의하세요. (로그인 5회 실패)");
        }
    }

    /**
     * 외부 사용자 계정 상태 검증
     */
    private void validateExternalUserAccount(ExternalUser user) {
        if (user.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.USER_DELETED, "삭제된 계정입니다.");
        }
        if (!user.getEmailVerified()) {
            throw new ApiException(ErrorCode.EMAIL_NOT_VERIFIED, "이메일 인증이 필요합니다.");
        }
        switch (user.getAccountStatus()) {
            case INACTIVE:
                throw new ApiException(ErrorCode.ACCOUNT_INACTIVE, "비활성화된 계정입니다.");
            case SUSPENDED:
                throw new ApiException(ErrorCode.ACCOUNT_SUSPENDED, "정지된 계정입니다.");
        }
    }

    /**
     * 로그인 성공 처리
     */
    private void handleLoginSuccess(User user, String ipAddress, String userAgent) {
        // 실패 횟수 초기화
        user.setFailCnt(0);
        userRepository.save(user);

        // 로그인 히스토리 기록
        LoginHistory history = LoginHistory.builder()
                .user(user)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(true)
                .loginAt(LocalDateTime.now())
                .build();
        loginHistoryRepository.save(history);

        log.info("로그인 성공: userId={}, ip={}", user.getUserId(), ipAddress);
    }

    /**
     * 로그인 실패 처리
     */
    private void handleLoginFailure(User user, String ipAddress, String userAgent) {
        // 실패 횟수 증가
        user.incrementFailCount();
        userRepository.save(user);

        // 로그인 히스토리 기록
        LoginHistory history = LoginHistory.builder()
                .user(user)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(false)
                .failureReason("Invalid password")
                .loginAt(LocalDateTime.now())
                .build();
        loginHistoryRepository.save(history);

        log.warn("로그인 실패: userId={}, ip={}, failCount={}",
                user.getUserId(), ipAddress, user.getFailCnt());
    }

    /**
     * 외부 사용자 로그인 성공 처리
     */
    private void handleExternalLoginSuccess(ExternalUser user, String ipAddress, String userAgent) {
        // 마지막 로그인 시간 업데이트
        user.setLastLoginAt(LocalDateTime.now());
        externalUserRepository.save(user);

        log.info("외부 사용자 로그인 성공: externalUserId={}, ip={}", user.getExternalUserId(), ipAddress);
    }

    /**
     * 외부 사용자 로그인 실패 처리
     */
    private void handleExternalLoginFailure(ExternalUser user, String ipAddress, String userAgent) {
        log.warn("외부 사용자 로그인 실패: externalUserId={}, ip={}", user.getExternalUserId(), ipAddress);
    }

    /**
     * 외부 사용자용 Access Token 생성
     */
    private String createExternalUserToken(ExternalUser user) {
        // ExternalUser는 User 엔티티가 아니므로 별도 처리 필요
        // 임시로 User 객체 생성하여 토큰 생성 (실제로는 별도 메서드 필요)
        return jwtTokenProvider.createAccessToken(
                User.builder()
                        .userId(user.getExternalUserId())
                        .studentNum(user.getEmail())
                        .email(user.getEmail())
                        .name(user.getName())
                        .role(com.scms.user.domain.enums.UserRole.EXTERNAL)
                        .build()
        );
    }

    /**
     * 외부 사용자용 Refresh Token 생성
     */
    private String createExternalUserRefreshToken(ExternalUser user) {
        return jwtTokenProvider.createRefreshToken(
                User.builder()
                        .userId(user.getExternalUserId())
                        .studentNum(user.getEmail())
                        .build()
        );
    }
}

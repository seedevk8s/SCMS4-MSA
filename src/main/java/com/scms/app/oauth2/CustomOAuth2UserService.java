package com.scms.app.oauth2;

import com.scms.app.model.ExternalUser;
import com.scms.app.oauth2.userinfo.OAuth2UserInfo;
import com.scms.app.oauth2.userinfo.OAuth2UserInfoFactory;
import com.scms.app.repository.ExternalUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

/**
 * OAuth2 사용자 서비스
 * - OAuth2 로그인 시 사용자 정보를 처리하고 DB에 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final ExternalUserRepository externalUserRepository;

    /**
     * OAuth2 사용자 정보 로드 및 처리
     */
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. OAuth2 사용자 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. registrationId 가져오기 (google, kakao, naver)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 로그인 시도: {}", registrationId);

        // 3. OAuth2 사용자 정보 파싱
        Map<String, Object> attributes = oAuth2User.getAttributes();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

        // 4. 필수 정보 검증
        String email = oAuth2UserInfo.getEmail();
        if (email == null || email.isEmpty()) {
            log.error("OAuth2 로그인 실패: 이메일 정보 없음 - {}", registrationId);
            throw new OAuth2AuthenticationException("이메일 정보를 가져올 수 없습니다.");
        }

        String providerId = oAuth2UserInfo.getProviderId();
        String provider = oAuth2UserInfo.getProvider();

        log.info("OAuth2 사용자 정보 - Provider: {}, ProviderId: {}, Email: {}", provider, providerId, email);

        // 5. 사용자 조회 또는 생성
        ExternalUser externalUser = externalUserRepository
                .findByProviderAndProviderIdAndDeletedAtIsNull(provider, providerId)
                .map(user -> updateExistingUser(user, oAuth2UserInfo))
                .orElseGet(() -> createNewUser(oAuth2UserInfo));

        // 6. 마지막 로그인 시간 업데이트
        externalUser.updateLastLogin();
        externalUserRepository.save(externalUser);

        log.info("OAuth2 로그인 성공 - UserId: {}, Email: {}", externalUser.getUserId(), externalUser.getEmail());

        // 7. CustomOAuth2User 반환 (Principal로 사용)
        return new CustomOAuth2User(externalUser, attributes);
    }

    /**
     * 기존 사용자 정보 업데이트
     */
    private ExternalUser updateExistingUser(ExternalUser existingUser, OAuth2UserInfo oAuth2UserInfo) {
        log.info("기존 사용자 정보 업데이트 - UserId: {}", existingUser.getUserId());

        // 이름과 프로필 이미지 업데이트
        existingUser.updateSocialInfo(
                oAuth2UserInfo.getName(),
                oAuth2UserInfo.getProfileImageUrl()
        );

        return existingUser;
    }

    /**
     * 새로운 소셜 로그인 사용자 생성
     */
    private ExternalUser createNewUser(OAuth2UserInfo oAuth2UserInfo) {
        log.info("새로운 소셜 로그인 사용자 생성 - Provider: {}, Email: {}",
                oAuth2UserInfo.getProvider(), oAuth2UserInfo.getEmail());

        ExternalUser newUser = ExternalUser.builder()
                .email(oAuth2UserInfo.getEmail())
                .password(null)  // 소셜 로그인 사용자는 비밀번호 없음
                .provider(oAuth2UserInfo.getProvider())
                .providerId(oAuth2UserInfo.getProviderId())
                .profileImageUrl(oAuth2UserInfo.getProfileImageUrl())
                .name(oAuth2UserInfo.getName())
                .birthDate(LocalDate.of(1900, 1, 1))  // 기본 생년월일 (추후 프로필에서 수정 가능)
                .emailVerified(true)  // 소셜 로그인은 이메일 인증 완료로 간주
                .agreeTerms(true)  // 소셜 로그인 시 약관 동의로 간주
                .agreePrivacy(true)  // 소셜 로그인 시 개인정보 처리방침 동의로 간주
                .agreeMarketing(false)
                .locked(false)
                .failCnt(0)
                .build();

        return externalUserRepository.save(newUser);
    }
}

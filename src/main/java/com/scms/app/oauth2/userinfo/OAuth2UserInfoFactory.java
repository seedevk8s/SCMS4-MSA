package com.scms.app.oauth2.userinfo;

import java.util.Map;

/**
 * OAuth2UserInfo 팩토리 클래스
 * - registrationId에 따라 적절한 OAuth2UserInfo 구현체 반환
 */
public class OAuth2UserInfoFactory {

    /**
     * registrationId에 따라 OAuth2UserInfo 구현체 생성
     *
     * @param registrationId OAuth2 제공자 ID (google, kakao, naver)
     * @param attributes     OAuth2 사용자 속성
     * @return OAuth2UserInfo 구현체
     */
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId == null) {
            throw new IllegalArgumentException("OAuth2 제공자 ID가 null입니다.");
        }

        switch (registrationId.toLowerCase()) {
            case "google":
                return new GoogleUserInfo(attributes);
            case "kakao":
                return new KakaoUserInfo(attributes);
            case "naver":
                return new NaverUserInfo(attributes);
            default:
                throw new IllegalArgumentException("지원하지 않는 OAuth2 제공자입니다: " + registrationId);
        }
    }
}

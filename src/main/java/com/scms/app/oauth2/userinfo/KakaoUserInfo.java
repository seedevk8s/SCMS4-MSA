package com.scms.app.oauth2.userinfo;

import java.util.Map;

/**
 * Kakao OAuth2 사용자 정보
 *
 * Kakao OAuth2 응답 예시:
 * {
 *   "id": 1234567890,
 *   "kakao_account": {
 *     "email": "user@kakao.com",
 *     "profile": {
 *       "nickname": "홍길동",
 *       "profile_image_url": "http://k.kakaocdn.net/..."
 *     }
 *   }
 * }
 */
public class KakaoUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getProvider() {
        return "KAKAO";
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            return null;
        }
        return (String) kakaoAccount.get("email");
    }

    @Override
    public String getName() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            return null;
        }
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        if (profile == null) {
            return null;
        }
        return (String) profile.get("nickname");
    }

    @Override
    public String getProfileImageUrl() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            return null;
        }
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        if (profile == null) {
            return null;
        }
        return (String) profile.get("profile_image_url");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}

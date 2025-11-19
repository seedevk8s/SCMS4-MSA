package com.scms.app.oauth2.userinfo;

import java.util.Map;

/**
 * Google OAuth2 사용자 정보
 *
 * Google OAuth2 응답 예시:
 * {
 *   "sub": "1234567890",
 *   "name": "홍길동",
 *   "email": "user@gmail.com",
 *   "picture": "https://lh3.googleusercontent.com/...",
 *   "email_verified": true
 * }
 */
public class GoogleUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GoogleUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getProvider() {
        return "GOOGLE";
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getProfileImageUrl() {
        return (String) attributes.get("picture");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}

package com.scms.app.oauth2.userinfo;

import java.util.Map;

/**
 * Naver OAuth2 사용자 정보
 *
 * Naver OAuth2 응답 예시:
 * {
 *   "resultcode": "00",
 *   "message": "success",
 *   "response": {
 *     "id": "1234567890",
 *     "email": "user@naver.com",
 *     "name": "홍길동",
 *     "profile_image": "https://phinf.pstatic.net/..."
 *   }
 * }
 */
public class NaverUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public NaverUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            return null;
        }
        return (String) response.get("id");
    }

    @Override
    public String getProvider() {
        return "NAVER";
    }

    @Override
    public String getEmail() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            return null;
        }
        return (String) response.get("email");
    }

    @Override
    public String getName() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            return null;
        }
        return (String) response.get("name");
    }

    @Override
    public String getProfileImageUrl() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            return null;
        }
        return (String) response.get("profile_image");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}

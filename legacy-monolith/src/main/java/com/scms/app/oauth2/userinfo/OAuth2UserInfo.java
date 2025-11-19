package com.scms.app.oauth2.userinfo;

import java.util.Map;

/**
 * OAuth2 사용자 정보 인터페이스
 * - 각 OAuth2 제공자(Google, Kakao, Naver)의 사용자 정보를 추상화
 */
public interface OAuth2UserInfo {

    /**
     * OAuth2 제공자 ID 반환
     */
    String getProviderId();

    /**
     * OAuth2 제공자 이름 반환
     */
    String getProvider();

    /**
     * 사용자 이메일 반환
     */
    String getEmail();

    /**
     * 사용자 이름 반환
     */
    String getName();

    /**
     * 프로필 이미지 URL 반환
     */
    String getProfileImageUrl();

    /**
     * 원본 사용자 속성 반환
     */
    Map<String, Object> getAttributes();
}

package com.scms.app.oauth2;

import com.scms.app.model.ExternalUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Custom OAuth2User 구현
 * - OAuth2 로그인 시 Principal로 사용되는 객체
 * - ExternalUser 정보를 포함
 */
@Getter
public class CustomOAuth2User implements OAuth2User {

    private final ExternalUser externalUser;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(ExternalUser externalUser, Map<String, Object> attributes) {
        this.externalUser = externalUser;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 외부 사용자는 ROLE_EXTERNAL_USER 권한 부여
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_EXTERNAL_USER"));
    }

    @Override
    public String getName() {
        return externalUser.getName();
    }

    /**
     * 사용자 ID 반환
     */
    public Integer getUserId() {
        return externalUser.getUserId();
    }

    /**
     * 이메일 반환
     */
    public String getEmail() {
        return externalUser.getEmail();
    }

    /**
     * Provider 반환
     */
    public String getProvider() {
        return externalUser.getProvider();
    }
}

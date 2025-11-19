package com.scms.app.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2 로그인 실패 핸들러
 * - OAuth2 로그인 실패 시 에러 메시지와 함께 로그인 페이지로 리다이렉트
 */
@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                       AuthenticationException exception) throws IOException, ServletException {

        log.error("OAuth2 로그인 실패: {}", exception.getMessage(), exception);

        // 에러 메시지와 함께 로그인 페이지로 리다이렉트
        String targetUrl = UriComponentsBuilder.fromUriString("/login")
                .queryParam("error", "oauth2")
                .queryParam("message", exception.getMessage())
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

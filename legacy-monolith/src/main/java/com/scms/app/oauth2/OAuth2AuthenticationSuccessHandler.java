package com.scms.app.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OAuth2 로그인 성공 핸들러
 * - OAuth2 로그인 성공 시 세션 처리 및 리다이렉트
 */
@Slf4j
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 1. CustomOAuth2User에서 사용자 정보 가져오기
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        // 2. 세션에 외부 사용자 정보 저장
        HttpSession session = request.getSession();
        session.setAttribute("externalUserId", oAuth2User.getUserId());
        session.setAttribute("externalUserEmail", oAuth2User.getEmail());
        session.setAttribute("externalUserName", oAuth2User.getName());
        session.setAttribute("userType", "EXTERNAL");
        session.setAttribute("provider", oAuth2User.getProvider());

        log.info("OAuth2 로그인 성공 - UserId: {}, Email: {}, Provider: {}",
                oAuth2User.getUserId(), oAuth2User.getEmail(), oAuth2User.getProvider());

        // 3. 메인 페이지로 리다이렉트
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            log.debug("응답이 이미 커밋되었습니다. {}로 리다이렉트할 수 없습니다.", targetUrl);
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * 리다이렉트 URL 결정
     */
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) {
        // 기본적으로 메인 페이지로 리다이렉트
        return "/";
    }
}

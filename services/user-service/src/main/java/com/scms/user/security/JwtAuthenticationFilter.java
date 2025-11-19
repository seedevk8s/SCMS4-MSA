package com.scms.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT 인증 필터
 *
 * 주요 기능:
 * - Authorization 헤더에서 JWT 토큰 추출
 * - 토큰 검증
 * - 인증 정보를 SecurityContext에 설정
 * - 사용자 ID를 request attribute로 전달
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. Authorization 헤더에서 JWT 토큰 추출
            String jwt = getJwtFromRequest(request);

            // 2. 토큰 검증
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {

                // 3. 토큰에서 사용자 정보 추출
                String studentNum = jwtTokenProvider.getStudentNum(jwt);
                Long userId = jwtTokenProvider.getUserId(jwt);
                String role = jwtTokenProvider.getRole(jwt);

                // 4. 권한 설정
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + role)
                );

                // 5. Authentication 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(studentNum, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 7. Request Attribute에 userId 저장 (Controller에서 사용)
                request.setAttribute("userId", userId);
                request.setAttribute("studentNum", studentNum);
                request.setAttribute("role", role);

                log.debug("JWT 인증 성공: studentNum={}, userId={}, role={}", studentNum, userId, role);
            }

        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생", e);
            // 인증 실패해도 필터 체인은 계속 진행 (SecurityContext에 인증 정보가 없으면 자동 거부됨)
        }

        // 8. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 JWT 토큰 추출
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer " 제거
        }

        return null;
    }

    /**
     * 특정 경로는 필터를 건너뜀
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Actuator, 로그인, 회원가입 등은 필터링 제외
        return path.startsWith("/actuator") ||
                path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/api/auth/password/reset") ||
                path.startsWith("/api/auth/verify-email") ||
                path.startsWith("/api/users/check");
    }
}

package com.scms.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS 설정
 * 웹 브라우저에서 다른 도메인의 API를 호출할 수 있도록 허용
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // 허용할 출처 (프로덕션에서는 특정 도메인으로 제한)
        corsConfig.setAllowedOrigins(List.of(
                "http://localhost:3000",  // React 개발 서버
                "http://localhost:8080",  // 로컬 프론트엔드
                "http://localhost:8081",  // 추가 프론트엔드
                "http://localhost:8000"   // 기타 개발 서버
        ));

        // 허용할 HTTP 메서드
        corsConfig.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // 허용할 헤더
        corsConfig.setAllowedHeaders(List.of("*"));

        // 인증 정보 포함 허용
        corsConfig.setAllowCredentials(true);

        // preflight 요청 캐시 시간 (초)
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}

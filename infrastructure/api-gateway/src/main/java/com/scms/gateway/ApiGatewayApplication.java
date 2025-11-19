package com.scms.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway Application
 * 모든 클라이언트 요청의 진입점
 *
 * 역할:
 * - 라우팅: 요청을 적절한 마이크로서비스로 전달
 * - 로드 밸런싱: 서비스 인스턴스 간 부하 분산
 * - 인증/인가: JWT 토큰 검증 (추후 구현)
 * - Rate Limiting: 요청 속도 제한
 * - CORS 처리
 * - Circuit Breaker: 장애 격리
 */
@EnableDiscoveryClient
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.java, args);
    }
}

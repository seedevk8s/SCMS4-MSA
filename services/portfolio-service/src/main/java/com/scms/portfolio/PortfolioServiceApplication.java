package com.scms.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Portfolio Service 애플리케이션
 *
 * 학생 포트폴리오 관리 마이크로서비스
 */
@EnableDiscoveryClient
@EnableJpaAuditing  // JPA Auditing 활성화 (createdAt, updatedAt 자동 관리)
@SpringBootApplication(
        scanBasePackages = {
                "com.scms.portfolio",
                "com.scms.common.exception"  // Common 모듈의 예외 처리
        }
)
public class PortfolioServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortfolioServiceApplication.class, args);
    }
}

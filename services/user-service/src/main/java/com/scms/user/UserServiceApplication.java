package com.scms.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * User Service Application
 * 사용자 관리 마이크로서비스
 *
 * 주요 기능:
 * - 사용자 등록, 조회, 수정, 삭제 (CRUD)
 * - 로그인 및 JWT 토큰 발급
 * - OAuth2 소셜 로그인 (Google, Kakao, Naver)
 * - 비밀번호 재설정
 * - 사용자 권한 관리
 */
@EnableDiscoveryClient
@EnableJpaAuditing  // JPA Auditing 활성화 (createdAt, updatedAt 자동 관리)
@SpringBootApplication(
        scanBasePackages = {
                "com.scms.user",
                "com.scms.common.exception"  // 공통 예외 핸들러 스캔
        }
)
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}

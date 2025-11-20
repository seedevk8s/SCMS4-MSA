package com.scms.survey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Survey Service 애플리케이션
 *
 * 설문조사 관리 마이크로서비스
 */
@EnableDiscoveryClient
@EnableJpaAuditing  // JPA Auditing 활성화
@SpringBootApplication(
        scanBasePackages = {
                "com.scms.survey",
                "com.scms.common.exception"  // Common 모듈의 예외 처리
        }
)
public class SurveyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SurveyServiceApplication.class, args);
    }
}

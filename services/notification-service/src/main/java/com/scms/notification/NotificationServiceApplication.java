package com.scms.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Notification Service Application
 * 알림 관리 마이크로서비스
 *
 * 주요 기능:
 * - 시스템 알림 CRUD
 * - 이메일 발송 (비밀번호 재설정, 프로그램 신청 결과 등)
 * - RabbitMQ 이벤트 구독 (다른 서비스로부터 알림 요청 수신)
 * - 알림 배치 전송
 * - 읽음/읽지않음 상태 관리
 */
@EnableDiscoveryClient
@EnableJpaAuditing
@SpringBootApplication(scanBasePackages = {
        "com.scms.notification",
        "com.scms.common.exception"  // 공통 예외 핸들러 스캔
})
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}

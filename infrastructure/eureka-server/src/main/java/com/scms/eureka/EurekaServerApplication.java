package com.scms.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server Application
 * 마이크로서비스 디스커버리 서버
 *
 * 역할:
 * - 마이크로서비스 등록 및 관리
 * - 서비스 인스턴스 정보 제공
 * - 헬스 체크 및 서비스 가용성 모니터링
 */
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}

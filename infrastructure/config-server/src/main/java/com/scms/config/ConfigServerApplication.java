package com.scms.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Config Server Application
 * 중앙 집중식 설정 관리 서버
 *
 * 역할:
 * - 모든 마이크로서비스의 설정 파일을 중앙에서 관리
 * - 환경별 설정 (dev, prod) 분리
 * - 설정 변경 시 서비스 재시작 없이 리로드 가능
 * - Git 저장소 또는 로컬 파일 시스템에서 설정 로드
 */
@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}

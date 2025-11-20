package com.scms.program;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableDiscoveryClient
@EnableJpaAuditing
@SpringBootApplication(
        scanBasePackages = {
                "com.scms.program",
                "com.scms.common.exception"
        }
)
public class ProgramServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProgramServiceApplication.class, args);
    }
}

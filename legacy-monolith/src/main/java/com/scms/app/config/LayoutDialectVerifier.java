package com.scms.app.config;

import jakarta.annotation.PostConstruct;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Arrays;

/**
 * LayoutDialect 등록 확인용 컴포넌트
 * 애플리케이션 시작 시 LayoutDialect가 제대로 등록되었는지 검증
 */
@Component
public class LayoutDialectVerifier {

    @Autowired
    private ApplicationContext context;

    @Autowired(required = false)
    private SpringTemplateEngine templateEngine;

    @PostConstruct
    public void verify() {
        System.out.println("========================================");
        System.out.println("=== LayoutDialect 검증 시작 ===");

        // 1. LayoutDialect Bean 존재 확인
        try {
            LayoutDialect dialect = context.getBean(LayoutDialect.class);
            System.out.println("✅ LayoutDialect Bean 발견: " + dialect.getClass().getName());
        } catch (Exception e) {
            System.out.println("❌ LayoutDialect Bean 없음: " + e.getMessage());
        }

        // 2. SpringTemplateEngine에 Dialect 등록 확인
        if (templateEngine != null) {
            boolean hasLayoutDialect = templateEngine.getDialects().stream()
                    .anyMatch(d -> d instanceof LayoutDialect);
            if (hasLayoutDialect) {
                System.out.println("✅ TemplateEngine에 LayoutDialect 등록됨");
            } else {
                System.out.println("❌ TemplateEngine에 LayoutDialect 미등록");
                System.out.println("등록된 Dialects: " + templateEngine.getDialects());
            }
        } else {
            System.out.println("❌ SpringTemplateEngine을 찾을 수 없음");
        }

        // 3. 모든 Bean 목록 출력
        String[] beanNames = context.getBeanNamesForType(LayoutDialect.class);
        System.out.println("LayoutDialect 타입 Bean 목록: " + Arrays.toString(beanNames));

        System.out.println("=== LayoutDialect 검증 완료 ===");
        System.out.println("========================================");
    }
}

package com.scms.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * API Gateway 라우팅 설정
 *
 * 각 마이크로서비스로의 라우팅 규칙을 정의합니다.
 * Eureka를 통한 동적 라우팅을 사용합니다.
 */
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service 라우팅
                .route("user-service", r -> r
                        .path("/api/users/**", "/api/auth/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("userServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/user-service"))
                                .retry(retryConfig -> retryConfig.setRetries(3)))
                        .uri("lb://user-service"))

                // Notification Service 라우팅
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("notificationServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/notification-service"))
                                .retry(retryConfig -> retryConfig.setRetries(2)))
                        .uri("lb://notification-service"))

                // Program Service 라우팅
                .route("program-service", r -> r
                        .path("/api/programs/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("programServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/program-service"))
                                .retry(retryConfig -> retryConfig.setRetries(3)))
                        .uri("lb://program-service"))

                // Program Application Service 라우팅
                .route("program-application-service", r -> r
                        .path("/api/program-applications/**", "/api/program-reviews/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("programApplicationServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/program-application-service")))
                        .uri("lb://program-application-service"))

                // Portfolio Service 라우팅
                .route("portfolio-service", r -> r
                        .path("/api/portfolios/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("portfolioServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/portfolio-service")))
                        .uri("lb://portfolio-service"))

                // Consultation Service 라우팅
                .route("consultation-service", r -> r
                        .path("/api/consultations/**", "/api/counselors/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("consultationServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/consultation-service")))
                        .uri("lb://consultation-service"))

                // Competency Service 라우팅
                .route("competency-service", r -> r
                        .path("/api/competencies/**", "/api/assessments/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("competencyServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/competency-service")))
                        .uri("lb://competency-service"))

                // Mileage Service 라우팅
                .route("mileage-service", r -> r
                        .path("/api/mileage/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("mileageServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/mileage-service")))
                        .uri("lb://mileage-service"))

                // Survey Service 라우팅
                .route("survey-service", r -> r
                        .path("/api/surveys/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("surveyServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/survey-service")))
                        .uri("lb://survey-service"))

                // External Employment Service 라우팅
                .route("external-employment-service", r -> r
                        .path("/api/external-employment/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("externalEmploymentServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/external-employment-service")))
                        .uri("lb://external-employment-service"))

                .build();
    }
}

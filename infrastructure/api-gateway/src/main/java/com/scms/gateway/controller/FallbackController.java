package com.scms.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Circuit Breaker Fallback Controller
 * 서비스 장애 시 대체 응답을 제공합니다.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user-service")
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        return createFallbackResponse("User Service");
    }

    @GetMapping("/notification-service")
    public ResponseEntity<Map<String, Object>> notificationServiceFallback() {
        return createFallbackResponse("Notification Service");
    }

    @GetMapping("/program-service")
    public ResponseEntity<Map<String, Object>> programServiceFallback() {
        return createFallbackResponse("Program Service");
    }

    @GetMapping("/program-application-service")
    public ResponseEntity<Map<String, Object>> programApplicationServiceFallback() {
        return createFallbackResponse("Program Application Service");
    }

    @GetMapping("/portfolio-service")
    public ResponseEntity<Map<String, Object>> portfolioServiceFallback() {
        return createFallbackResponse("Portfolio Service");
    }

    @GetMapping("/consultation-service")
    public ResponseEntity<Map<String, Object>> consultationServiceFallback() {
        return createFallbackResponse("Consultation Service");
    }

    @GetMapping("/competency-service")
    public ResponseEntity<Map<String, Object>> competencyServiceFallback() {
        return createFallbackResponse("Competency Service");
    }

    @GetMapping("/mileage-service")
    public ResponseEntity<Map<String, Object>> mileageServiceFallback() {
        return createFallbackResponse("Mileage Service");
    }

    @GetMapping("/survey-service")
    public ResponseEntity<Map<String, Object>> surveyServiceFallback() {
        return createFallbackResponse("Survey Service");
    }

    @GetMapping("/external-employment-service")
    public ResponseEntity<Map<String, Object>> externalEmploymentServiceFallback() {
        return createFallbackResponse("External Employment Service");
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String serviceName) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", serviceName + "가 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요.");
        response.put("errorCode", "SERVICE_UNAVAILABLE");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}

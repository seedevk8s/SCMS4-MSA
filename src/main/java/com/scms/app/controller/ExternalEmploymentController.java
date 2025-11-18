package com.scms.app.controller;

import com.scms.app.dto.external.*;
import com.scms.app.model.EmploymentType;
import com.scms.app.service.ExternalEmploymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 외부취업 활동 REST API Controller
 */
@RestController
@RequestMapping("/api/external-employments")
@RequiredArgsConstructor
@Slf4j
public class ExternalEmploymentController {

    private final ExternalEmploymentService employmentService;

    /**
     * 외부취업 활동 등록
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ExternalEmploymentResponse> registerEmployment(
            @Valid @RequestBody ExternalEmploymentRequest request,
            Authentication authentication) {

        Integer userId = (Integer) authentication.getPrincipal();
        log.info("외부취업 활동 등록 API 호출 - userId: {}", userId);

        ExternalEmploymentResponse response = employmentService.registerEmployment(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 외부취업 활동 수정
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ExternalEmploymentResponse> updateEmployment(
            @PathVariable("id") Long employmentId,
            @Valid @RequestBody ExternalEmploymentRequest request,
            Authentication authentication) {

        Integer userId = (Integer) authentication.getPrincipal();
        log.info("외부취업 활동 수정 API 호출 - employmentId: {}, userId: {}", employmentId, userId);

        ExternalEmploymentResponse response = employmentService.updateEmployment(employmentId, userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 외부취업 활동 삭제
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> deleteEmployment(
            @PathVariable("id") Long employmentId,
            Authentication authentication) {

        Integer userId = (Integer) authentication.getPrincipal();
        log.info("외부취업 활동 삭제 API 호출 - employmentId: {}, userId: {}", employmentId, userId);

        employmentService.deleteEmployment(employmentId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 내 외부취업 활동 목록 조회
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ExternalEmploymentResponse>> getMyEmployments(
            Authentication authentication) {

        Integer userId = (Integer) authentication.getPrincipal();
        log.info("내 외부취업 활동 목록 조회 API 호출 - userId: {}", userId);

        List<ExternalEmploymentResponse> employments = employmentService.getEmploymentsByUserId(userId);
        return ResponseEntity.ok(employments);
    }

    /**
     * 외부취업 활동 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExternalEmploymentResponse> getEmploymentById(
            @PathVariable("id") Long employmentId) {

        log.info("외부취업 활동 상세 조회 API 호출 - employmentId: {}", employmentId);

        ExternalEmploymentResponse employment = employmentService.getEmploymentById(employmentId);
        return ResponseEntity.ok(employment);
    }

    /**
     * 내 총 획득 가점 조회
     */
    @GetMapping("/my/total-credits")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Integer>> getMyTotalCredits(
            Authentication authentication) {

        Integer userId = (Integer) authentication.getPrincipal();
        log.info("내 총 획득 가점 조회 API 호출 - userId: {}", userId);

        Integer totalCredits = employmentService.getTotalCreditsByUserId(userId);
        Map<String, Integer> response = new HashMap<>();
        response.put("totalCredits", totalCredits);

        return ResponseEntity.ok(response);
    }

    /**
     * 관리자: 승인 대기 중인 활동 목록 조회
     */
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ExternalEmploymentResponse>> getPendingEmployments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("승인 대기 중인 활동 목록 조회 API 호출 - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<ExternalEmploymentResponse> employments = employmentService.getPendingEmployments(pageable);

        return ResponseEntity.ok(employments);
    }

    /**
     * 관리자: 승인된 활동 목록 조회
     */
    @GetMapping("/admin/verified")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ExternalEmploymentResponse>> getVerifiedEmployments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("승인된 활동 목록 조회 API 호출 - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("verificationDate").descending());
        Page<ExternalEmploymentResponse> employments = employmentService.getVerifiedEmployments(pageable);

        return ResponseEntity.ok(employments);
    }

    /**
     * 관리자: 활동 승인/거절
     */
    @PostMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExternalEmploymentResponse> verifyEmployment(
            @PathVariable("id") Long employmentId,
            @Valid @RequestBody ExternalEmploymentVerifyRequest request,
            Authentication authentication) {

        Integer adminId = (Integer) authentication.getPrincipal();
        log.info("외부취업 활동 검토 API 호출 - employmentId: {}, adminId: {}, approve: {}",
                employmentId, adminId, request.getApprove());

        ExternalEmploymentResponse response = employmentService.verifyEmployment(employmentId, adminId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 가점 자동 계산 미리보기
     */
    @GetMapping("/calculate-credits")
    public ResponseEntity<Map<String, Integer>> calculateCredits(
            @RequestParam EmploymentType employmentType,
            @RequestParam Integer durationMonths) {

        log.info("가점 계산 API 호출 - type: {}, duration: {}", employmentType, durationMonths);

        Integer credits = employmentService.calculateCredits(employmentType, durationMonths);
        Map<String, Integer> response = new HashMap<>();
        response.put("credits", credits);

        return ResponseEntity.ok(response);
    }

    /**
     * 관리자: 통계 조회
     */
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExternalEmploymentStatisticsResponse> getStatistics() {
        log.info("외부취업 활동 통계 조회 API 호출");

        ExternalEmploymentStatisticsResponse statistics = employmentService.getStatistics();
        return ResponseEntity.ok(statistics);
    }
}

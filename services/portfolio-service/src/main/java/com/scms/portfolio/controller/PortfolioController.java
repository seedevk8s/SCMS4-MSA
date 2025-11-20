package com.scms.portfolio.controller;

import com.scms.common.dto.ApiResponse;
import com.scms.portfolio.domain.enums.PortfolioStatus;
import com.scms.portfolio.domain.enums.VisibilityLevel;
import com.scms.portfolio.dto.request.PortfolioCreateRequest;
import com.scms.portfolio.dto.request.PortfolioUpdateRequest;
import com.scms.portfolio.dto.response.PortfolioResponse;
import com.scms.portfolio.service.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 포트폴리오 컨트롤러
 *
 * 포트폴리오 관련 REST API 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    /**
     * 포트폴리오 생성
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PortfolioResponse> createPortfolio(
            @Valid @RequestBody PortfolioCreateRequest request,
            @RequestHeader("X-User-Id") Long userId
    ) {
        PortfolioResponse response = portfolioService.createPortfolio(request, userId);
        return ApiResponse.success(response);
    }

    /**
     * 포트폴리오 수정
     */
    @PutMapping("/{portfolioId}")
    public ApiResponse<PortfolioResponse> updatePortfolio(
            @PathVariable Long portfolioId,
            @Valid @RequestBody PortfolioUpdateRequest request,
            @RequestHeader("X-User-Id") Long userId
    ) {
        PortfolioResponse response = portfolioService.updatePortfolio(portfolioId, request, userId);
        return ApiResponse.success(response);
    }

    /**
     * 포트폴리오 삭제
     */
    @DeleteMapping("/{portfolioId}")
    public ApiResponse<Void> deletePortfolio(
            @PathVariable Long portfolioId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        portfolioService.deletePortfolio(portfolioId, userId);
        return ApiResponse.success();
    }

    /**
     * 포트폴리오 상세 조회
     */
    @GetMapping("/{portfolioId}")
    public ApiResponse<PortfolioResponse> getPortfolio(
            @PathVariable Long portfolioId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        PortfolioResponse response = portfolioService.getPortfolio(portfolioId, userId);
        return ApiResponse.success(response);
    }

    /**
     * 사용자별 포트폴리오 목록 조회
     */
    @GetMapping("/users/{userId}")
    public ApiResponse<List<PortfolioResponse>> getUserPortfolios(
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") Long currentUserId
    ) {
        List<PortfolioResponse> responses = portfolioService.getUserPortfolios(userId, currentUserId);
        return ApiResponse.success(responses);
    }

    /**
     * 내 포트폴리오 목록 조회
     */
    @GetMapping("/my")
    public ApiResponse<List<PortfolioResponse>> getMyPortfolios(
            @RequestHeader("X-User-Id") Long userId
    ) {
        List<PortfolioResponse> responses = portfolioService.getUserPortfolios(userId, userId);
        return ApiResponse.success(responses);
    }

    /**
     * 공개 포트폴리오 목록 조회
     */
    @GetMapping("/public")
    public ApiResponse<List<PortfolioResponse>> getPublicPortfolios() {
        List<PortfolioResponse> responses = portfolioService.getPublicPortfolios();
        return ApiResponse.success(responses);
    }

    /**
     * 인기 포트폴리오 목록 조회
     */
    @GetMapping("/popular")
    public ApiResponse<List<PortfolioResponse>> getPopularPortfolios() {
        List<PortfolioResponse> responses = portfolioService.getPopularPortfolios();
        return ApiResponse.success(responses);
    }

    /**
     * 추천 포트폴리오 목록 조회
     */
    @GetMapping("/recommended")
    public ApiResponse<List<PortfolioResponse>> getRecommendedPortfolios() {
        List<PortfolioResponse> responses = portfolioService.getRecommendedPortfolios();
        return ApiResponse.success(responses);
    }

    /**
     * 포트폴리오 검색
     */
    @GetMapping("/search")
    public ApiResponse<List<PortfolioResponse>> searchPortfolios(
            @RequestParam String keyword
    ) {
        List<PortfolioResponse> responses = portfolioService.searchPortfolios(keyword);
        return ApiResponse.success(responses);
    }

    /**
     * 포트폴리오 상태 변경
     */
    @PatchMapping("/{portfolioId}/status")
    public ApiResponse<Void> updateStatus(
            @PathVariable Long portfolioId,
            @RequestParam PortfolioStatus status,
            @RequestHeader("X-User-Id") Long userId
    ) {
        portfolioService.updateStatus(portfolioId, status, userId);
        return ApiResponse.success();
    }

    /**
     * 포트폴리오 공개 범위 변경
     */
    @PatchMapping("/{portfolioId}/visibility")
    public ApiResponse<Void> updateVisibility(
            @PathVariable Long portfolioId,
            @RequestParam VisibilityLevel visibility,
            @RequestHeader("X-User-Id") Long userId
    ) {
        portfolioService.updateVisibility(portfolioId, visibility, userId);
        return ApiResponse.success();
    }

    /**
     * 포트폴리오 좋아요
     */
    @PostMapping("/{portfolioId}/like")
    public ApiResponse<Void> likePortfolio(
            @PathVariable Long portfolioId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        portfolioService.likePortfolio(portfolioId, userId);
        return ApiResponse.success();
    }

    /**
     * 포트폴리오 좋아요 취소
     */
    @DeleteMapping("/{portfolioId}/like")
    public ApiResponse<Void> unlikePortfolio(
            @PathVariable Long portfolioId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        portfolioService.unlikePortfolio(portfolioId, userId);
        return ApiResponse.success();
    }

    /**
     * 포트폴리오 공유
     */
    @PostMapping("/{portfolioId}/share")
    public ApiResponse<Void> sharePortfolio(@PathVariable Long portfolioId) {
        portfolioService.sharePortfolio(portfolioId);
        return ApiResponse.success();
    }

    /**
     * 프로필 이미지 업데이트
     */
    @PatchMapping("/{portfolioId}/profile-image")
    public ApiResponse<Void> updateProfileImage(
            @PathVariable Long portfolioId,
            @RequestParam String imageUrl,
            @RequestHeader("X-User-Id") Long userId
    ) {
        portfolioService.updateProfileImage(portfolioId, imageUrl, userId);
        return ApiResponse.success();
    }

    /**
     * 커버 이미지 업데이트
     */
    @PatchMapping("/{portfolioId}/cover-image")
    public ApiResponse<Void> updateCoverImage(
            @PathVariable Long portfolioId,
            @RequestParam String imageUrl,
            @RequestHeader("X-User-Id") Long userId
    ) {
        portfolioService.updateCoverImage(portfolioId, imageUrl, userId);
        return ApiResponse.success();
    }
}

package com.scms.portfolio.controller;

import com.scms.common.dto.ApiResponse;
import com.scms.portfolio.domain.enums.PortfolioType;
import com.scms.portfolio.dto.request.PortfolioItemCreateRequest;
import com.scms.portfolio.dto.request.PortfolioItemUpdateRequest;
import com.scms.portfolio.dto.response.PortfolioItemResponse;
import com.scms.portfolio.service.PortfolioItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 포트폴리오 항목 컨트롤러
 *
 * 포트폴리오 항목 관련 REST API 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/portfolios/{portfolioId}/items")
@RequiredArgsConstructor
public class PortfolioItemController {

    private final PortfolioItemService portfolioItemService;

    /**
     * 포트폴리오 항목 생성
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PortfolioItemResponse> createItem(
            @PathVariable Long portfolioId,
            @Valid @RequestBody PortfolioItemCreateRequest request,
            @RequestHeader("X-User-Id") Long userId
    ) {
        PortfolioItemResponse response = portfolioItemService.createItem(portfolioId, request, userId);
        return ApiResponse.success(response);
    }

    /**
     * 포트폴리오 항목 수정
     */
    @PutMapping("/{itemId}")
    public ApiResponse<PortfolioItemResponse> updateItem(
            @PathVariable Long portfolioId,
            @PathVariable Long itemId,
            @Valid @RequestBody PortfolioItemUpdateRequest request,
            @RequestHeader("X-User-Id") Long userId
    ) {
        PortfolioItemResponse response = portfolioItemService.updateItem(itemId, request, userId);
        return ApiResponse.success(response);
    }

    /**
     * 포트폴리오 항목 삭제
     */
    @DeleteMapping("/{itemId}")
    public ApiResponse<Void> deleteItem(
            @PathVariable Long portfolioId,
            @PathVariable Long itemId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        portfolioItemService.deleteItem(itemId, userId);
        return ApiResponse.success();
    }

    /**
     * 포트폴리오 항목 상세 조회
     */
    @GetMapping("/{itemId}")
    public ApiResponse<PortfolioItemResponse> getItem(
            @PathVariable Long portfolioId,
            @PathVariable Long itemId
    ) {
        PortfolioItemResponse response = portfolioItemService.getItem(itemId);
        return ApiResponse.success(response);
    }

    /**
     * 포트폴리오의 모든 항목 조회
     */
    @GetMapping
    public ApiResponse<List<PortfolioItemResponse>> getPortfolioItems(
            @PathVariable Long portfolioId
    ) {
        List<PortfolioItemResponse> responses = portfolioItemService.getPortfolioItems(portfolioId);
        return ApiResponse.success(responses);
    }

    /**
     * 타입별 항목 조회
     */
    @GetMapping("/type/{type}")
    public ApiResponse<List<PortfolioItemResponse>> getItemsByType(
            @PathVariable Long portfolioId,
            @PathVariable PortfolioType type
    ) {
        List<PortfolioItemResponse> responses = portfolioItemService.getItemsByType(portfolioId, type);
        return ApiResponse.success(responses);
    }

    /**
     * 강조 표시된 항목 조회
     */
    @GetMapping("/featured")
    public ApiResponse<List<PortfolioItemResponse>> getFeaturedItems(
            @PathVariable Long portfolioId
    ) {
        List<PortfolioItemResponse> responses = portfolioItemService.getFeaturedItems(portfolioId);
        return ApiResponse.success(responses);
    }

    /**
     * 진행 중인 항목 조회
     */
    @GetMapping("/ongoing")
    public ApiResponse<List<PortfolioItemResponse>> getOngoingItems(
            @PathVariable Long portfolioId
    ) {
        List<PortfolioItemResponse> responses = portfolioItemService.getOngoingItems(portfolioId);
        return ApiResponse.success(responses);
    }

    /**
     * 강조 표시 토글
     */
    @PatchMapping("/{itemId}/featured")
    public ApiResponse<Void> toggleFeatured(
            @PathVariable Long portfolioId,
            @PathVariable Long itemId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        portfolioItemService.toggleFeatured(itemId, userId);
        return ApiResponse.success();
    }

    /**
     * 썸네일 이미지 업데이트
     */
    @PatchMapping("/{itemId}/thumbnail")
    public ApiResponse<Void> updateThumbnail(
            @PathVariable Long portfolioId,
            @PathVariable Long itemId,
            @RequestParam String thumbnailUrl,
            @RequestHeader("X-User-Id") Long userId
    ) {
        portfolioItemService.updateThumbnail(itemId, thumbnailUrl, userId);
        return ApiResponse.success();
    }

    /**
     * 항목 순서 변경
     */
    @PatchMapping("/{itemId}/order")
    public ApiResponse<Void> updateDisplayOrder(
            @PathVariable Long portfolioId,
            @PathVariable Long itemId,
            @RequestParam Integer displayOrder,
            @RequestHeader("X-User-Id") Long userId
    ) {
        portfolioItemService.updateDisplayOrder(itemId, displayOrder, userId);
        return ApiResponse.success();
    }

    /**
     * 기술 스택으로 검색
     */
    @GetMapping("/search")
    public ApiResponse<List<PortfolioItemResponse>> searchByTechStack(
            @PathVariable Long portfolioId,
            @RequestParam String techStack
    ) {
        List<PortfolioItemResponse> responses = portfolioItemService.searchByTechStack(portfolioId, techStack);
        return ApiResponse.success(responses);
    }
}

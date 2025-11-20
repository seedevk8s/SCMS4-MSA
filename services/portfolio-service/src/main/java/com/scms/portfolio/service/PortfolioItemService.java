package com.scms.portfolio.service;

import com.scms.common.exception.ApiException;
import com.scms.common.exception.ErrorCode;
import com.scms.portfolio.domain.entity.Portfolio;
import com.scms.portfolio.domain.entity.PortfolioItem;
import com.scms.portfolio.domain.enums.PortfolioType;
import com.scms.portfolio.dto.request.PortfolioItemCreateRequest;
import com.scms.portfolio.dto.request.PortfolioItemUpdateRequest;
import com.scms.portfolio.dto.response.PortfolioItemResponse;
import com.scms.portfolio.repository.PortfolioItemRepository;
import com.scms.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 포트폴리오 항목 서비스
 *
 * 주요 기능:
 * - 포트폴리오 항목 생성, 수정, 삭제, 조회
 * - 항목 정렬 순서 관리
 * - 강조 표시 관리
 * - 타입별 항목 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioItemService {

    private final PortfolioItemRepository portfolioItemRepository;
    private final PortfolioRepository portfolioRepository;

    /**
     * 포트폴리오 항목 생성
     */
    @Transactional
    public PortfolioItemResponse createItem(Long portfolioId, PortfolioItemCreateRequest request, Long userId) {
        Portfolio portfolio = getPortfolioEntity(portfolioId);

        // 권한 확인
        validateOwnership(portfolio, userId);

        PortfolioItem item = PortfolioItem.builder()
                .portfolio(portfolio)
                .type(request.getType())
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .ongoing(request.getOngoing())
                .role(request.getRole())
                .techStack(request.getTechStack())
                .url(request.getUrl())
                .repositoryUrl(request.getRepositoryUrl())
                .achievement(request.getAchievement())
                .displayOrder(request.getDisplayOrder())
                .featured(request.getFeatured())
                .build();

        PortfolioItem saved = portfolioItemRepository.save(item);
        log.info("포트폴리오 항목 생성: itemId={}, portfolioId={}", saved.getItemId(), portfolioId);

        return PortfolioItemResponse.from(saved);
    }

    /**
     * 포트폴리오 항목 수정
     */
    @Transactional
    public PortfolioItemResponse updateItem(Long itemId, PortfolioItemUpdateRequest request, Long userId) {
        PortfolioItem item = getItemEntity(itemId);

        // 권한 확인
        validateOwnership(item.getPortfolio(), userId);

        item.update(
                request.getTitle(),
                request.getSubtitle(),
                request.getDescription(),
                request.getStartDate(),
                request.getEndDate(),
                request.getOngoing(),
                request.getRole(),
                request.getTechStack(),
                request.getUrl(),
                request.getRepositoryUrl(),
                request.getAchievement()
        );

        if (request.getDisplayOrder() != null) {
            item.updateDisplayOrder(request.getDisplayOrder());
        }

        if (request.getFeatured() != null && item.getFeatured() != request.getFeatured()) {
            item.toggleFeatured();
        }

        log.info("포트폴리오 항목 수정: itemId={}", itemId);
        return PortfolioItemResponse.from(item);
    }

    /**
     * 포트폴리오 항목 삭제
     */
    @Transactional
    public void deleteItem(Long itemId, Long userId) {
        PortfolioItem item = getItemEntity(itemId);

        // 권한 확인
        validateOwnership(item.getPortfolio(), userId);

        item.markAsDeleted();
        log.info("포트폴리오 항목 삭제: itemId={}", itemId);
    }

    /**
     * 포트폴리오 항목 상세 조회
     */
    public PortfolioItemResponse getItem(Long itemId) {
        PortfolioItem item = getItemEntity(itemId);
        return PortfolioItemResponse.from(item);
    }

    /**
     * 포트폴리오의 모든 항목 조회
     */
    public List<PortfolioItemResponse> getPortfolioItems(Long portfolioId) {
        return portfolioItemRepository.findByPortfolio_PortfolioIdAndDeletedAtIsNullOrderByDisplayOrderAsc(portfolioId)
                .stream()
                .map(PortfolioItemResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 타입별 항목 조회
     */
    public List<PortfolioItemResponse> getItemsByType(Long portfolioId, PortfolioType type) {
        return portfolioItemRepository.findByPortfolio_PortfolioIdAndTypeAndDeletedAtIsNullOrderByDisplayOrderAsc(
                        portfolioId, type)
                .stream()
                .map(PortfolioItemResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 강조 표시된 항목 조회
     */
    public List<PortfolioItemResponse> getFeaturedItems(Long portfolioId) {
        return portfolioItemRepository.findByPortfolio_PortfolioIdAndFeaturedTrueAndDeletedAtIsNullOrderByDisplayOrderAsc(
                        portfolioId)
                .stream()
                .map(PortfolioItemResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 진행 중인 항목 조회
     */
    public List<PortfolioItemResponse> getOngoingItems(Long portfolioId) {
        return portfolioItemRepository.findByPortfolio_PortfolioIdAndOngoingTrueAndDeletedAtIsNullOrderByStartDateDesc(
                        portfolioId)
                .stream()
                .map(PortfolioItemResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 강조 표시 토글
     */
    @Transactional
    public void toggleFeatured(Long itemId, Long userId) {
        PortfolioItem item = getItemEntity(itemId);

        // 권한 확인
        validateOwnership(item.getPortfolio(), userId);

        item.toggleFeatured();
        log.info("포트폴리오 항목 강조 표시 토글: itemId={}, featured={}", itemId, item.getFeatured());
    }

    /**
     * 썸네일 이미지 업데이트
     */
    @Transactional
    public void updateThumbnail(Long itemId, String thumbnailUrl, Long userId) {
        PortfolioItem item = getItemEntity(itemId);

        // 권한 확인
        validateOwnership(item.getPortfolio(), userId);

        item.updateThumbnail(thumbnailUrl);
        log.info("포트폴리오 항목 썸네일 업데이트: itemId={}", itemId);
    }

    /**
     * 항목 순서 변경
     */
    @Transactional
    public void updateDisplayOrder(Long itemId, Integer displayOrder, Long userId) {
        PortfolioItem item = getItemEntity(itemId);

        // 권한 확인
        validateOwnership(item.getPortfolio(), userId);

        item.updateDisplayOrder(displayOrder);
        log.info("포트폴리오 항목 순서 변경: itemId={}, order={}", itemId, displayOrder);
    }

    /**
     * 기술 스택으로 항목 검색
     */
    public List<PortfolioItemResponse> searchByTechStack(Long portfolioId, String techStack) {
        return portfolioItemRepository.searchByTechStack(portfolioId, techStack)
                .stream()
                .map(PortfolioItemResponse::from)
                .collect(Collectors.toList());
    }

    // ===== Private 메서드 =====

    /**
     * 포트폴리오 엔티티 조회
     */
    private Portfolio getPortfolioEntity(Long portfolioId) {
        return portfolioRepository.findByPortfolioIdAndDeletedAtIsNull(portfolioId)
                .orElseThrow(() -> new ApiException(ErrorCode.PORTFOLIO_NOT_FOUND));
    }

    /**
     * 항목 엔티티 조회
     */
    private PortfolioItem getItemEntity(Long itemId) {
        return portfolioItemRepository.findByItemIdAndDeletedAtIsNull(itemId)
                .orElseThrow(() -> new ApiException(ErrorCode.PORTFOLIO_ITEM_NOT_FOUND));
    }

    /**
     * 소유권 검증
     */
    private void validateOwnership(Portfolio portfolio, Long userId) {
        if (!portfolio.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "본인의 포트폴리오만 수정/삭제할 수 있습니다.");
        }
    }
}

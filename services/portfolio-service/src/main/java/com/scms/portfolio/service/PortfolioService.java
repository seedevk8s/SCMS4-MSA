package com.scms.portfolio.service;

import com.scms.common.exception.ApiException;
import com.scms.common.exception.ErrorCode;
import com.scms.portfolio.domain.entity.Portfolio;
import com.scms.portfolio.domain.enums.PortfolioStatus;
import com.scms.portfolio.domain.enums.VisibilityLevel;
import com.scms.portfolio.dto.request.PortfolioCreateRequest;
import com.scms.portfolio.dto.request.PortfolioUpdateRequest;
import com.scms.portfolio.dto.response.PortfolioResponse;
import com.scms.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 포트폴리오 서비스
 *
 * 주요 기능:
 * - 포트폴리오 생성, 수정, 삭제, 조회
 * - 상태 및 공개 범위 관리
 * - 조회수, 좋아요, 공유 관리
 * - 공개 포트폴리오 검색
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    /**
     * 포트폴리오 생성
     */
    @Transactional
    public PortfolioResponse createPortfolio(PortfolioCreateRequest request, Long userId) {
        Portfolio portfolio = Portfolio.builder()
                .userId(userId)
                .title(request.getTitle())
                .introduction(request.getIntroduction())
                .status(PortfolioStatus.DRAFT)
                .visibilityLevel(request.getVisibilityLevel() != null ?
                        request.getVisibilityLevel() : VisibilityLevel.PRIVATE)
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .githubUrl(request.getGithubUrl())
                .linkedinUrl(request.getLinkedinUrl())
                .websiteUrl(request.getWebsiteUrl())
                .build();

        Portfolio saved = portfolioRepository.save(portfolio);
        log.info("포트폴리오 생성: portfolioId={}, userId={}", saved.getPortfolioId(), userId);

        return PortfolioResponse.from(saved);
    }

    /**
     * 포트폴리오 수정
     */
    @Transactional
    public PortfolioResponse updatePortfolio(Long portfolioId, PortfolioUpdateRequest request, Long userId) {
        Portfolio portfolio = getPortfolioEntity(portfolioId);

        // 권한 확인
        validateOwnership(portfolio, userId);

        // 정보 업데이트
        portfolio.update(
                request.getTitle(),
                request.getIntroduction(),
                request.getContactEmail(),
                request.getContactPhone(),
                request.getGithubUrl(),
                request.getLinkedinUrl(),
                request.getWebsiteUrl()
        );

        // 상태 업데이트
        if (request.getStatus() != null) {
            portfolio.updateStatus(request.getStatus());
        }

        // 공개 범위 업데이트
        if (request.getVisibilityLevel() != null) {
            portfolio.updateVisibility(request.getVisibilityLevel());
        }

        log.info("포트폴리오 수정: portfolioId={}", portfolioId);
        return PortfolioResponse.from(portfolio);
    }

    /**
     * 포트폴리오 삭제 (Soft Delete)
     */
    @Transactional
    public void deletePortfolio(Long portfolioId, Long userId) {
        Portfolio portfolio = getPortfolioEntity(portfolioId);

        // 권한 확인
        validateOwnership(portfolio, userId);

        portfolio.markAsDeleted();
        log.info("포트폴리오 삭제: portfolioId={}", portfolioId);
    }

    /**
     * 포트폴리오 상세 조회
     */
    @Transactional
    public PortfolioResponse getPortfolio(Long portfolioId, Long currentUserId) {
        Portfolio portfolio = getPortfolioEntity(portfolioId);

        // 조회 권한 확인
        validateViewPermission(portfolio, currentUserId);

        // 조회수 증가 (본인 제외)
        if (!portfolio.getUserId().equals(currentUserId)) {
            portfolioRepository.incrementViewCount(portfolioId);
        }

        return PortfolioResponse.from(portfolio);
    }

    /**
     * 사용자의 포트폴리오 목록 조회
     */
    public List<PortfolioResponse> getUserPortfolios(Long userId, Long currentUserId) {
        // 본인이면 모두 조회, 타인이면 공개된 것만 조회
        if (userId.equals(currentUserId)) {
            return portfolioRepository.findByUserIdAndDeletedAtIsNullOrderByUpdatedAtDesc(userId)
                    .stream()
                    .map(PortfolioResponse::fromWithoutItems)
                    .collect(Collectors.toList());
        } else {
            return portfolioRepository.findByUserIdAndStatusAndDeletedAtIsNullOrderByUpdatedAtDesc(
                            userId, PortfolioStatus.PUBLISHED)
                    .stream()
                    .filter(p -> p.getVisibilityLevel() == VisibilityLevel.PUBLIC)
                    .map(PortfolioResponse::fromWithoutItems)
                    .collect(Collectors.toList());
        }
    }

    /**
     * 공개 포트폴리오 목록 조회 (최신순)
     */
    public List<PortfolioResponse> getPublicPortfolios() {
        return portfolioRepository.findPublicPortfolios()
                .stream()
                .map(PortfolioResponse::fromWithoutItems)
                .collect(Collectors.toList());
    }

    /**
     * 인기 포트폴리오 목록 조회
     */
    public List<PortfolioResponse> getPopularPortfolios() {
        return portfolioRepository.findPopularPortfolios()
                .stream()
                .map(PortfolioResponse::fromWithoutItems)
                .collect(Collectors.toList());
    }

    /**
     * 추천 포트폴리오 목록 조회
     */
    public List<PortfolioResponse> getRecommendedPortfolios() {
        return portfolioRepository.findRecommendedPortfolios()
                .stream()
                .map(PortfolioResponse::fromWithoutItems)
                .collect(Collectors.toList());
    }

    /**
     * 포트폴리오 검색 (제목)
     */
    public List<PortfolioResponse> searchPortfolios(String keyword) {
        return portfolioRepository.searchByTitle(keyword)
                .stream()
                .map(PortfolioResponse::fromWithoutItems)
                .collect(Collectors.toList());
    }

    /**
     * 포트폴리오 상태 변경
     */
    @Transactional
    public void updateStatus(Long portfolioId, PortfolioStatus status, Long userId) {
        Portfolio portfolio = getPortfolioEntity(portfolioId);

        // 권한 확인
        validateOwnership(portfolio, userId);

        portfolio.updateStatus(status);
        log.info("포트폴리오 상태 변경: portfolioId={}, status={}", portfolioId, status);
    }

    /**
     * 포트폴리오 공개 범위 변경
     */
    @Transactional
    public void updateVisibility(Long portfolioId, VisibilityLevel visibilityLevel, Long userId) {
        Portfolio portfolio = getPortfolioEntity(portfolioId);

        // 권한 확인
        validateOwnership(portfolio, userId);

        portfolio.updateVisibility(visibilityLevel);
        log.info("포트폴리오 공개 범위 변경: portfolioId={}, visibility={}", portfolioId, visibilityLevel);
    }

    /**
     * 포트폴리오 좋아요
     */
    @Transactional
    public void likePortfolio(Long portfolioId, Long userId) {
        Portfolio portfolio = getPortfolioEntity(portfolioId);

        // 본인 포트폴리오는 좋아요 불가
        if (portfolio.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "본인의 포트폴리오는 좋아요할 수 없습니다.");
        }

        portfolioRepository.incrementLikeCount(portfolioId);
        log.info("포트폴리오 좋아요: portfolioId={}, userId={}", portfolioId, userId);
    }

    /**
     * 포트폴리오 좋아요 취소
     */
    @Transactional
    public void unlikePortfolio(Long portfolioId, Long userId) {
        Portfolio portfolio = getPortfolioEntity(portfolioId);

        portfolioRepository.decrementLikeCount(portfolioId);
        log.info("포트폴리오 좋아요 취소: portfolioId={}, userId={}", portfolioId, userId);
    }

    /**
     * 포트폴리오 공유
     */
    @Transactional
    public void sharePortfolio(Long portfolioId) {
        Portfolio portfolio = getPortfolioEntity(portfolioId);

        portfolioRepository.incrementShareCount(portfolioId);
        log.info("포트폴리오 공유: portfolioId={}", portfolioId);
    }

    /**
     * 프로필 이미지 업데이트
     */
    @Transactional
    public void updateProfileImage(Long portfolioId, String imageUrl, Long userId) {
        Portfolio portfolio = getPortfolioEntity(portfolioId);

        // 권한 확인
        validateOwnership(portfolio, userId);

        portfolio.updateProfileImage(imageUrl);
        log.info("프로필 이미지 업데이트: portfolioId={}", portfolioId);
    }

    /**
     * 커버 이미지 업데이트
     */
    @Transactional
    public void updateCoverImage(Long portfolioId, String imageUrl, Long userId) {
        Portfolio portfolio = getPortfolioEntity(portfolioId);

        // 권한 확인
        validateOwnership(portfolio, userId);

        portfolio.updateCoverImage(imageUrl);
        log.info("커버 이미지 업데이트: portfolioId={}", portfolioId);
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
     * 소유권 검증
     */
    private void validateOwnership(Portfolio portfolio, Long userId) {
        if (!portfolio.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "본인의 포트폴리오만 수정/삭제할 수 있습니다.");
        }
    }

    /**
     * 조회 권한 검증
     */
    private void validateViewPermission(Portfolio portfolio, Long currentUserId) {
        // 본인이면 무조건 조회 가능
        if (portfolio.getUserId().equals(currentUserId)) {
            return;
        }

        // 공개 상태가 아니면 조회 불가
        if (portfolio.getStatus() != PortfolioStatus.PUBLISHED) {
            throw new ApiException(ErrorCode.FORBIDDEN, "공개되지 않은 포트폴리오입니다.");
        }

        // 비공개면 조회 불가
        if (portfolio.getVisibilityLevel() == VisibilityLevel.PRIVATE) {
            throw new ApiException(ErrorCode.FORBIDDEN, "비공개 포트폴리오입니다.");
        }
    }
}

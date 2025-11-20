package com.scms.portfolio.repository;

import com.scms.portfolio.domain.entity.PortfolioItem;
import com.scms.portfolio.domain.enums.PortfolioType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 포트폴리오 항목 Repository
 */
@Repository
public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {

    /**
     * 포트폴리오별 항목 목록 (순서대로)
     */
    List<PortfolioItem> findByPortfolio_PortfolioIdAndDeletedAtIsNullOrderByDisplayOrderAsc(Long portfolioId);

    /**
     * 포트폴리오 + 타입별 항목 목록
     */
    List<PortfolioItem> findByPortfolio_PortfolioIdAndTypeAndDeletedAtIsNullOrderByDisplayOrderAsc(
            Long portfolioId,
            PortfolioType type
    );

    /**
     * 강조 표시된 항목 목록
     */
    List<PortfolioItem> findByPortfolio_PortfolioIdAndFeaturedTrueAndDeletedAtIsNullOrderByDisplayOrderAsc(
            Long portfolioId
    );

    /**
     * 항목 상세 조회
     */
    Optional<PortfolioItem> findByItemIdAndDeletedAtIsNull(Long itemId);

    /**
     * 포트폴리오별 항목 개수
     */
    long countByPortfolio_PortfolioIdAndDeletedAtIsNull(Long portfolioId);

    /**
     * 포트폴리오 + 타입별 항목 개수
     */
    long countByPortfolio_PortfolioIdAndTypeAndDeletedAtIsNull(Long portfolioId, PortfolioType type);

    /**
     * 진행 중인 항목 조회
     */
    List<PortfolioItem> findByPortfolio_PortfolioIdAndOngoingTrueAndDeletedAtIsNullOrderByStartDateDesc(
            Long portfolioId
    );

    /**
     * 사용자의 모든 항목 (포트폴리오 무관)
     */
    @Query("SELECT i FROM PortfolioItem i WHERE i.portfolio.userId = :userId " +
            "AND i.deletedAt IS NULL " +
            "ORDER BY i.startDate DESC")
    List<PortfolioItem> findAllByUserId(@Param("userId") Long userId);

    /**
     * 특정 기술 스택을 포함한 항목 검색
     */
    @Query("SELECT i FROM PortfolioItem i WHERE i.portfolio.portfolioId = :portfolioId " +
            "AND i.techStack LIKE %:techStack% " +
            "AND i.deletedAt IS NULL " +
            "ORDER BY i.displayOrder ASC")
    List<PortfolioItem> searchByTechStack(@Param("portfolioId") Long portfolioId,
                                          @Param("techStack") String techStack);
}

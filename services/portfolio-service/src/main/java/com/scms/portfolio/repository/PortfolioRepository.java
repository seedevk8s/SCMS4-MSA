package com.scms.portfolio.repository;

import com.scms.portfolio.domain.entity.Portfolio;
import com.scms.portfolio.domain.enums.PortfolioStatus;
import com.scms.portfolio.domain.enums.VisibilityLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 포트폴리오 Repository
 */
@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    /**
     * 사용자별 포트폴리오 목록 조회
     */
    List<Portfolio> findByUserIdAndDeletedAtIsNullOrderByUpdatedAtDesc(Long userId);

    /**
     * 사용자 + 상태별 포트폴리오 목록
     */
    List<Portfolio> findByUserIdAndStatusAndDeletedAtIsNullOrderByUpdatedAtDesc(
            Long userId,
            PortfolioStatus status
    );

    /**
     * 공개 포트폴리오 목록 (최신순)
     */
    @Query("SELECT p FROM Portfolio p WHERE p.status = 'PUBLISHED' " +
            "AND p.visibilityLevel = 'PUBLIC' " +
            "AND p.deletedAt IS NULL " +
            "ORDER BY p.publishedAt DESC")
    List<Portfolio> findPublicPortfolios();

    /**
     * 인기 포트폴리오 목록 (조회수 순)
     */
    @Query("SELECT p FROM Portfolio p WHERE p.status = 'PUBLISHED' " +
            "AND p.visibilityLevel = 'PUBLIC' " +
            "AND p.deletedAt IS NULL " +
            "ORDER BY p.viewCount DESC, p.likeCount DESC")
    List<Portfolio> findPopularPortfolios();

    /**
     * 추천 포트폴리오 목록 (좋아요 순)
     */
    @Query("SELECT p FROM Portfolio p WHERE p.status = 'PUBLISHED' " +
            "AND p.visibilityLevel = 'PUBLIC' " +
            "AND p.deletedAt IS NULL " +
            "ORDER BY p.likeCount DESC, p.viewCount DESC")
    List<Portfolio> findRecommendedPortfolios();

    /**
     * 최근 공개된 포트폴리오 (N일 이내)
     */
    @Query("SELECT p FROM Portfolio p WHERE p.status = 'PUBLISHED' " +
            "AND p.visibilityLevel = 'PUBLIC' " +
            "AND p.publishedAt >= :sinceDate " +
            "AND p.deletedAt IS NULL " +
            "ORDER BY p.publishedAt DESC")
    List<Portfolio> findRecentlyPublishedPortfolios(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * 포트폴리오 상세 조회 (삭제되지 않은 것만)
     */
    Optional<Portfolio> findByPortfolioIdAndDeletedAtIsNull(Long portfolioId);

    /**
     * 사용자의 공개된 포트폴리오 조회
     */
    Optional<Portfolio> findByUserIdAndStatusAndDeletedAtIsNull(Long userId, PortfolioStatus status);

    /**
     * 사용자별 포트폴리오 개수
     */
    long countByUserIdAndDeletedAtIsNull(Long userId);

    /**
     * 사용자별 공개 포트폴리오 개수
     */
    long countByUserIdAndStatusAndDeletedAtIsNull(Long userId, PortfolioStatus status);

    /**
     * 조회수 증가
     */
    @Modifying
    @Query("UPDATE Portfolio p SET p.viewCount = p.viewCount + 1 WHERE p.portfolioId = :portfolioId")
    void incrementViewCount(@Param("portfolioId") Long portfolioId);

    /**
     * 좋아요 수 증가
     */
    @Modifying
    @Query("UPDATE Portfolio p SET p.likeCount = p.likeCount + 1 WHERE p.portfolioId = :portfolioId")
    void incrementLikeCount(@Param("portfolioId") Long portfolioId);

    /**
     * 좋아요 수 감소
     */
    @Modifying
    @Query("UPDATE Portfolio p SET p.likeCount = p.likeCount - 1 WHERE p.portfolioId = :portfolioId AND p.likeCount > 0")
    void decrementLikeCount(@Param("portfolioId") Long portfolioId);

    /**
     * 공유 수 증가
     */
    @Modifying
    @Query("UPDATE Portfolio p SET p.shareCount = p.shareCount + 1 WHERE p.portfolioId = :portfolioId")
    void incrementShareCount(@Param("portfolioId") Long portfolioId);

    /**
     * 특정 공개 범위의 포트폴리오 조회
     */
    List<Portfolio> findByVisibilityLevelAndStatusAndDeletedAtIsNullOrderByUpdatedAtDesc(
            VisibilityLevel visibilityLevel,
            PortfolioStatus status
    );

    /**
     * 검색 - 제목으로 검색 (공개 포트폴리오만)
     */
    @Query("SELECT p FROM Portfolio p WHERE p.status = 'PUBLISHED' " +
            "AND p.visibilityLevel = 'PUBLIC' " +
            "AND p.title LIKE %:keyword% " +
            "AND p.deletedAt IS NULL " +
            "ORDER BY p.publishedAt DESC")
    List<Portfolio> searchByTitle(@Param("keyword") String keyword);
}

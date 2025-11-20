package com.scms.portfolio.domain.entity;

import com.scms.portfolio.domain.enums.PortfolioStatus;
import com.scms.portfolio.domain.enums.VisibilityLevel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 포트폴리오 엔티티
 *
 * 사용자의 포트폴리오 메인 정보를 저장합니다.
 * 각 포트폴리오는 여러 개의 포트폴리오 항목(PortfolioItem)을 가질 수 있습니다.
 */
@Entity
@Table(name = "portfolios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long portfolioId;

    /**
     * 사용자 ID (User Service의 userId)
     * MSA 환경에서는 FK 제약조건 없이 ID만 저장
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 포트폴리오 제목
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 포트폴리오 소개/설명
     */
    @Column(columnDefinition = "TEXT")
    private String introduction;

    /**
     * 상태 (DRAFT, PUBLISHED, ARCHIVED)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PortfolioStatus status = PortfolioStatus.DRAFT;

    /**
     * 공개 범위 (PUBLIC, PRIVATE, COMPANY_ONLY, SCHOOL_ONLY)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VisibilityLevel visibilityLevel = VisibilityLevel.PRIVATE;

    /**
     * 프로필 이미지 URL
     */
    @Column(length = 500)
    private String profileImageUrl;

    /**
     * 대표 이미지 URL
     */
    @Column(length = 500)
    private String coverImageUrl;

    /**
     * 연락처 (이메일)
     */
    @Column(length = 100)
    private String contactEmail;

    /**
     * 연락처 (전화번호)
     */
    @Column(length = 20)
    private String contactPhone;

    /**
     * GitHub URL
     */
    @Column(length = 200)
    private String githubUrl;

    /**
     * LinkedIn URL
     */
    @Column(length = 200)
    private String linkedinUrl;

    /**
     * 개인 웹사이트/블로그 URL
     */
    @Column(length = 200)
    private String websiteUrl;

    /**
     * 조회수
     */
    @Column(nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    /**
     * 좋아요 수
     */
    @Column(nullable = false)
    @Builder.Default
    private Long likeCount = 0L;

    /**
     * 공유 수
     */
    @Column(nullable = false)
    @Builder.Default
    private Long shareCount = 0L;

    /**
     * 마지막 공개일
     */
    private LocalDateTime publishedAt;

    /**
     * 포트폴리오 항목 목록
     */
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PortfolioItem> items = new ArrayList<>();

    /**
     * 생성일시
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 삭제일시 (Soft Delete)
     */
    private LocalDateTime deletedAt;

    // ===== 비즈니스 메서드 =====

    /**
     * 포트폴리오 정보 수정
     */
    public void update(String title, String introduction, String contactEmail, String contactPhone,
                      String githubUrl, String linkedinUrl, String websiteUrl) {
        this.title = title;
        this.introduction = introduction;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.githubUrl = githubUrl;
        this.linkedinUrl = linkedinUrl;
        this.websiteUrl = websiteUrl;
    }

    /**
     * 상태 변경
     */
    public void updateStatus(PortfolioStatus status) {
        this.status = status;
        if (status == PortfolioStatus.PUBLISHED) {
            this.publishedAt = LocalDateTime.now();
        }
    }

    /**
     * 공개 범위 변경
     */
    public void updateVisibility(VisibilityLevel visibilityLevel) {
        this.visibilityLevel = visibilityLevel;
    }

    /**
     * 프로필 이미지 업데이트
     */
    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * 커버 이미지 업데이트
     */
    public void updateCoverImage(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    /**
     * 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 좋아요 증가
     */
    public void incrementLikeCount() {
        this.likeCount++;
    }

    /**
     * 좋아요 감소
     */
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    /**
     * 공유 수 증가
     */
    public void incrementShareCount() {
        this.shareCount++;
    }

    /**
     * 포트폴리오 항목 추가
     */
    public void addItem(PortfolioItem item) {
        this.items.add(item);
        item.setPortfolio(this);
    }

    /**
     * 포트폴리오 항목 제거
     */
    public void removeItem(PortfolioItem item) {
        this.items.remove(item);
        item.setPortfolio(null);
    }

    /**
     * 공개 여부 확인
     */
    public boolean isPublished() {
        return this.status == PortfolioStatus.PUBLISHED;
    }

    /**
     * 공개 가능 여부 확인
     */
    public boolean isPublic() {
        return this.visibilityLevel == VisibilityLevel.PUBLIC && isPublished();
    }

    /**
     * Soft Delete
     */
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 복원
     */
    public void restore() {
        this.deletedAt = null;
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}

package com.scms.portfolio.domain.entity;

import com.scms.portfolio.domain.enums.PortfolioType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 포트폴리오 항목 엔티티
 *
 * 포트폴리오의 개별 항목 (프로젝트, 수상, 자격증 등)을 저장합니다.
 */
@Entity
@Table(name = "portfolio_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PortfolioItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    /**
     * 포트폴리오 참조
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    /**
     * 항목 타입 (PROJECT, AWARD, CERTIFICATE, ACTIVITY 등)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PortfolioType type;

    /**
     * 제목
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 부제목/소속 (예: 회사명, 학교명, 주최기관 등)
     */
    @Column(length = 200)
    private String subtitle;

    /**
     * 설명
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 시작일
     */
    private LocalDate startDate;

    /**
     * 종료일
     */
    private LocalDate endDate;

    /**
     * 진행 중 여부 (종료일이 없는 경우)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean ongoing = false;

    /**
     * 역할/직책
     */
    @Column(length = 100)
    private String role;

    /**
     * 사용 기술 스택 (쉼표로 구분)
     */
    @Column(length = 500)
    private String techStack;

    /**
     * 프로젝트/활동 URL
     */
    @Column(length = 500)
    private String url;

    /**
     * GitHub 저장소 URL
     */
    @Column(length = 500)
    private String repositoryUrl;

    /**
     * 성과/결과
     */
    @Column(columnDefinition = "TEXT")
    private String achievement;

    /**
     * 순서 (정렬용)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * 강조 표시 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean featured = false;

    /**
     * 썸네일 이미지 URL
     */
    @Column(length = 500)
    private String thumbnailUrl;

    /**
     * 첨부 파일 목록
     */
    @OneToMany(mappedBy = "portfolioItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PortfolioAttachment> attachments = new ArrayList<>();

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

    // ===== 연관관계 편의 메서드 =====

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    // ===== 비즈니스 메서드 =====

    /**
     * 항목 정보 수정
     */
    public void update(String title, String subtitle, String description,
                      LocalDate startDate, LocalDate endDate, Boolean ongoing,
                      String role, String techStack, String url, String repositoryUrl,
                      String achievement) {
        this.title = title;
        this.subtitle = subtitle;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.ongoing = ongoing;
        this.role = role;
        this.techStack = techStack;
        this.url = url;
        this.repositoryUrl = repositoryUrl;
        this.achievement = achievement;
    }

    /**
     * 썸네일 이미지 업데이트
     */
    public void updateThumbnail(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    /**
     * 순서 변경
     */
    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    /**
     * 강조 표시 토글
     */
    public void toggleFeatured() {
        this.featured = !this.featured;
    }

    /**
     * 첨부 파일 추가
     */
    public void addAttachment(PortfolioAttachment attachment) {
        this.attachments.add(attachment);
        attachment.setPortfolioItem(this);
    }

    /**
     * 첨부 파일 제거
     */
    public void removeAttachment(PortfolioAttachment attachment) {
        this.attachments.remove(attachment);
        attachment.setPortfolioItem(null);
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

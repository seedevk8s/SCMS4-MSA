package com.scms.portfolio.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 포트폴리오 첨부 파일 엔티티
 *
 * 포트폴리오 항목의 첨부 파일 정보를 저장합니다.
 */
@Entity
@Table(name = "portfolio_attachments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PortfolioAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private Long attachmentId;

    /**
     * 포트폴리오 항목 참조
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private PortfolioItem portfolioItem;

    /**
     * 원본 파일명
     */
    @Column(nullable = false, length = 255)
    private String originalFilename;

    /**
     * 저장된 파일명 (UUID 등)
     */
    @Column(nullable = false, length = 255)
    private String storedFilename;

    /**
     * 파일 경로/URL
     */
    @Column(nullable = false, length = 500)
    private String fileUrl;

    /**
     * 파일 타입 (MIME Type)
     */
    @Column(nullable = false, length = 100)
    private String fileType;

    /**
     * 파일 크기 (bytes)
     */
    @Column(nullable = false)
    private Long fileSize;

    /**
     * 순서 (정렬용)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * 생성일시
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 삭제일시 (Soft Delete)
     */
    private LocalDateTime deletedAt;

    // ===== 연관관계 편의 메서드 =====

    public void setPortfolioItem(PortfolioItem portfolioItem) {
        this.portfolioItem = portfolioItem;
    }

    // ===== 비즈니스 메서드 =====

    /**
     * 순서 변경
     */
    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    /**
     * 이미지 파일 여부 확인
     */
    public boolean isImage() {
        return fileType != null && fileType.startsWith("image/");
    }

    /**
     * PDF 파일 여부 확인
     */
    public boolean isPdf() {
        return "application/pdf".equals(fileType);
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

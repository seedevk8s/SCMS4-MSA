package com.scms.user.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상담사 엔티티
 * - User 엔티티와 1:1 관계 (ROLE=COUNSELOR인 사용자)
 * - 상담사의 전문 분야 및 소개 정보 관리
 */
@Entity
@Table(name = "counselors", indexes = {
    @Index(name = "idx_counselor_deleted_at", columnList = "deleted_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Counselor {

    @Id
    @Column(name = "counselor_id")
    private Integer counselorId;

    /**
     * User와 1:1 관계
     * - User의 PK를 공유 (Shared Primary Key)
     * - User의 role이 COUNSELOR인 경우에만 Counselor 레코드 생성
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "counselor_id")
    private User user;

    /**
     * 전문 분야
     */
    @Size(max = 100, message = "전문 분야는 100자 이하여야 합니다")
    @Column(name = "specialty", length = 100)
    private String specialty;

    /**
     * 상담사 소개
     */
    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction;

    /**
     * 생성일시
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 삭제일시 (Soft Delete)
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Soft Delete
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * 전문 분야 업데이트
     */
    public void updateSpecialty(String specialty) {
        this.specialty = specialty;
    }

    /**
     * 소개 업데이트
     */
    public void updateIntroduction(String introduction) {
        this.introduction = introduction;
    }
}

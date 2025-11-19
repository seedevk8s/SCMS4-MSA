package com.scms.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 프로그램 신청 엔티티
 */
@Entity
@Table(name = "program_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Integer applicationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(name = "applied_at", nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.appliedAt == null) {
            this.appliedAt = LocalDateTime.now();
        }
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
     * 신청 승인
     */
    public void approve() {
        this.status = ApplicationStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * 신청 거부
     */
    public void reject(String reason) {
        this.status = ApplicationStatus.REJECTED;
        this.rejectedAt = LocalDateTime.now();
        this.rejectionReason = reason;
    }

    /**
     * 신청 취소
     */
    public void cancel() {
        this.status = ApplicationStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    /**
     * 참여 완료 처리
     */
    public void complete() {
        this.status = ApplicationStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 취소 가능 여부 확인
     */
    public boolean isCancellable() {
        return (status == ApplicationStatus.PENDING || status == ApplicationStatus.APPROVED)
                && !isDeleted();
    }
}

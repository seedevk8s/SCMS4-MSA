package com.scms.program.domain.entity;

import com.scms.program.domain.enums.ApplicationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 프로그램 신청 Entity
 *
 * 주요 기능:
 * - 학생의 프로그램 신청 관리
 * - 신청 승인/거절
 * - 출석 체크
 * - 수료 관리
 */
@Entity
@Table(name = "program_applications", indexes = {
        @Index(name = "idx_program_id", columnList = "program_id"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_program_user", columnList = "program_id,user_id", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId;

    /**
     * 프로그램 ID
     */
    @NotNull
    @Column(name = "program_id", nullable = false)
    private Long programId;

    /**
     * 신청자 User ID (User Service의 userId)
     */
    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 신청자 이름 (비정규화 - 조회 성능)
     */
    @Size(max = 100)
    @Column(name = "user_name", length = 100)
    private String userName;

    /**
     * 신청자 학번 (비정규화)
     */
    @Size(max = 20)
    @Column(name = "student_num", length = 20)
    private String studentNum;

    /**
     * 신청 상태
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    /**
     * 신청 동기
     */
    @Size(max = 1000)
    @Column(name = "motivation", length = 1000, columnDefinition = "TEXT")
    private String motivation;

    /**
     * 승인/거절 사유
     */
    @Size(max = 500)
    @Column(name = "review_comment", length = 500)
    private String reviewComment;

    /**
     * 승인/거절 일시
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /**
     * 승인/거절한 관리자 ID
     */
    @Column(name = "reviewed_by")
    private Long reviewedBy;

    /**
     * 출석 여부
     */
    @Builder.Default
    @Column(name = "attended", nullable = false)
    private Boolean attended = false;

    /**
     * 출석 일시
     */
    @Column(name = "attended_at")
    private LocalDateTime attendedAt;

    /**
     * 수료 여부
     */
    @Builder.Default
    @Column(name = "completed", nullable = false)
    private Boolean completed = false;

    /**
     * 수료 일시
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * 생성 일시 (신청 일시)
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 삭제 일시 (취소)
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ==================== Business Methods ====================

    /**
     * 승인
     */
    public void approve(Long reviewedBy, String comment) {
        this.status = ApplicationStatus.APPROVED;
        this.reviewedBy = reviewedBy;
        this.reviewComment = comment;
        this.reviewedAt = LocalDateTime.now();
    }

    /**
     * 거절
     */
    public void reject(Long reviewedBy, String comment) {
        this.status = ApplicationStatus.REJECTED;
        this.reviewedBy = reviewedBy;
        this.reviewComment = comment;
        this.reviewedAt = LocalDateTime.now();
    }

    /**
     * 취소
     */
    public void cancel() {
        this.status = ApplicationStatus.CANCELLED;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 출석 체크
     */
    public void markAttended() {
        this.attended = true;
        this.attendedAt = LocalDateTime.now();
        this.status = ApplicationStatus.ATTENDED;
    }

    /**
     * 수료 처리
     */
    public void markCompleted() {
        this.completed = true;
        this.completedAt = LocalDateTime.now();
        this.status = ApplicationStatus.COMPLETED;
    }

    /**
     * 승인 여부
     */
    public boolean isApproved() {
        return this.status == ApplicationStatus.APPROVED
                || this.status == ApplicationStatus.ATTENDED
                || this.status == ApplicationStatus.COMPLETED;
    }

    /**
     * 취소 여부
     */
    public boolean isCancelled() {
        return this.deletedAt != null || this.status == ApplicationStatus.CANCELLED;
    }
}

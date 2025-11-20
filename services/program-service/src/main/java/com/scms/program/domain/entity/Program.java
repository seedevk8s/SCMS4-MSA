package com.scms.program.domain.entity;

import com.scms.program.domain.enums.ProgramStatus;
import com.scms.program.domain.enums.ProgramType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 비교과 프로그램 Entity
 *
 * 주요 기능:
 * - 프로그램 정보 관리
 * - 모집 정원 관리
 * - 신청 마감일 관리
 * - 프로그램 상태 관리
 */
@Entity
@Table(name = "programs", indexes = {
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_category_id", columnList = "category_id"),
        @Index(name = "idx_start_date", columnList = "start_date"),
        @Index(name = "idx_created_by", columnList = "created_by")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "program_id")
    private Long programId;

    /**
     * 프로그램 제목
     */
    @NotBlank
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 프로그램 설명
     */
    @NotBlank
    @Size(max = 5000)
    @Column(name = "description", nullable = false, length = 5000, columnDefinition = "TEXT")
    private String description;

    /**
     * 프로그램 유형
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ProgramType type;

    /**
     * 프로그램 상태
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private ProgramStatus status = ProgramStatus.DRAFT;

    /**
     * 카테고리 ID
     */
    @Column(name = "category_id")
    private Long categoryId;

    /**
     * 시작 일시
     */
    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /**
     * 종료 일시
     */
    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /**
     * 신청 시작 일시
     */
    @Column(name = "application_start_date")
    private LocalDateTime applicationStartDate;

    /**
     * 신청 마감 일시
     */
    @Column(name = "application_end_date")
    private LocalDateTime applicationEndDate;

    /**
     * 모집 정원
     */
    @NotNull
    @Min(1)
    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    /**
     * 현재 신청 인원
     */
    @Builder.Default
    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants = 0;

    /**
     * 장소
     */
    @Size(max = 200)
    @Column(name = "location", length = 200)
    private String location;

    /**
     * 담당자 User ID (User Service의 userId)
     */
    @NotNull
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    /**
     * 담당자 이름 (비정규화 - 조회 성능)
     */
    @Size(max = 100)
    @Column(name = "instructor_name", length = 100)
    private String instructorName;

    /**
     * 담당자 연락처
     */
    @Size(max = 20)
    @Column(name = "instructor_contact", length = 20)
    private String instructorContact;

    /**
     * 마일리지 점수
     */
    @Builder.Default
    @Column(name = "mileage_points")
    private Integer mileagePoints = 0;

    /**
     * 썸네일 이미지 URL
     */
    @Size(max = 500)
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    /**
     * 조회수
     */
    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    /**
     * 생성 일시
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
     * 삭제 일시 (Soft Delete)
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ==================== Business Methods ====================

    /**
     * 신청 인원 증가
     */
    public void incrementParticipants() {
        if (this.currentParticipants >= this.maxParticipants) {
            throw new IllegalStateException("정원이 초과되었습니다.");
        }
        this.currentParticipants++;
    }

    /**
     * 신청 인원 감소
     */
    public void decrementParticipants() {
        if (this.currentParticipants > 0) {
            this.currentParticipants--;
        }
    }

    /**
     * 정원 초과 여부
     */
    public boolean isFull() {
        return this.currentParticipants >= this.maxParticipants;
    }

    /**
     * 신청 가능 여부
     */
    public boolean isApplicationAvailable() {
        LocalDateTime now = LocalDateTime.now();
        return this.status == ProgramStatus.APPROVED
                && !isFull()
                && (applicationStartDate == null || now.isAfter(applicationStartDate))
                && (applicationEndDate == null || now.isBefore(applicationEndDate));
    }

    /**
     * 신청 마감 여부
     */
    public boolean isApplicationClosed() {
        LocalDateTime now = LocalDateTime.now();
        return isFull()
                || (applicationEndDate != null && now.isAfter(applicationEndDate))
                || this.status == ProgramStatus.CLOSED;
    }

    /**
     * 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 삭제 처리 (Soft Delete)
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 삭제 여부
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * 승인
     */
    public void approve() {
        this.status = ProgramStatus.APPROVED;
    }

    /**
     * 마감
     */
    public void close() {
        this.status = ProgramStatus.CLOSED;
    }

    /**
     * 완료
     */
    public void complete() {
        this.status = ProgramStatus.COMPLETED;
    }

    /**
     * 취소
     */
    public void cancel() {
        this.status = ProgramStatus.CANCELLED;
    }
}

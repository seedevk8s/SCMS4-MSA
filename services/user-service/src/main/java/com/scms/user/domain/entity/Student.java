package com.scms.user.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 학생 엔티티
 * - User 테이블과는 별도로 학생 정보를 저장
 * - 레거시 데이터 호환성을 위한 별도 테이블
 */
@Entity
@Table(name = "students", indexes = {
    @Index(name = "idx_student_student_id", columnList = "student_id"),
    @Index(name = "idx_student_email", columnList = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 학번
     */
    @NotBlank(message = "학번은 필수입니다")
    @Size(max = 50, message = "학번은 50자 이하여야 합니다")
    @Column(name = "student_id", nullable = false, length = 50)
    private String studentId;

    /**
     * 이름
     */
    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 100, message = "이름은 100자 이하여야 합니다")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 이메일
     */
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다")
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    /**
     * 전화번호
     */
    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다")
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 학과
     */
    @Size(max = 100, message = "학과는 100자 이하여야 합니다")
    @Column(name = "department", length = 100)
    private String department;

    /**
     * 학년
     */
    @Size(max = 20, message = "학년은 20자 이하여야 합니다")
    @Column(name = "grade", length = 20)
    private String grade;

    /**
     * 재학 상태 (재학, 휴학, 졸업 등)
     */
    @Size(max = 20, message = "상태는 20자 이하여야 합니다")
    @Column(name = "status", length = 20)
    private String status;

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
}

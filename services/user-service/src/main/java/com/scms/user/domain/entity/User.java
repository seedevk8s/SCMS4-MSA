package com.scms.user.domain.entity;

import com.scms.user.domain.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사용자 엔티티
 * - 학생, 상담사, 관리자 통합 관리 (내부 회원)
 * - 학번으로 로그인하는 재학생 및 교직원
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_student_num", columnList = "student_num"),
    @Index(name = "idx_user_deleted_at", columnList = "deleted_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    /**
     * 학번 (로그인 ID로 사용)
     */
    @NotNull(message = "학번은 필수입니다")
    @Column(name = "student_num", nullable = false, unique = true)
    private Integer studentNum;

    /**
     * 이름
     */
    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다")
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * 이메일
     */
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * 전화번호
     */
    @Size(max = 100, message = "전화번호는 100자 이하여야 합니다")
    @Column(name = "phone", length = 100)
    private String phone;

    /**
     * 비밀번호 (암호화 저장)
     */
    @Size(max = 255, message = "비밀번호는 255자 이하여야 합니다")
    @Column(name = "password", length = 255)
    private String password;

    /**
     * 생년월일
     */
    @NotNull(message = "생년월일은 필수입니다")
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    /**
     * 학과/부서
     */
    @Size(max = 100, message = "학과/부서는 100자 이하여야 합니다")
    @Column(name = "department", length = 100)
    private String department;

    /**
     * 학년
     */
    @Min(value = 1, message = "학년은 1 이상이어야 합니다")
    @Max(value = 6, message = "학년은 6 이하여야 합니다")
    @Column(name = "grade")
    private Integer grade;

    /**
     * 사용자 역할 (STUDENT, COUNSELOR, ADMIN)
     */
    @NotNull(message = "역할은 필수입니다")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    @Builder.Default
    private UserRole role = UserRole.STUDENT;

    /**
     * 계정 잠금 여부
     */
    @NotNull
    @Column(name = "locked", nullable = false)
    @Builder.Default
    private Boolean locked = false;

    /**
     * 로그인 실패 횟수
     */
    @NotNull
    @Min(value = 0, message = "실패 횟수는 0 이상이어야 합니다")
    @Column(name = "fail_cnt", nullable = false)
    @Builder.Default
    private Integer failCnt = 0;

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
     * 계정 잠금
     */
    public void lock() {
        this.locked = true;
    }

    /**
     * 계정 잠금 해제
     */
    public void unlock() {
        this.locked = false;
        this.failCnt = 0;
    }

    /**
     * 로그인 실패 횟수 증가
     * - 5회 이상 실패 시 자동 잠금
     */
    public void incrementFailCount() {
        this.failCnt++;
        if (this.failCnt >= 5) {
            this.lock();
        }
    }

    /**
     * 로그인 성공 시 실패 횟수 초기화
     */
    public void resetFailCount() {
        this.failCnt = 0;
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * 계정 잠금 여부 확인
     */
    public boolean isLocked() {
        return this.locked != null && this.locked;
    }

    /**
     * 활성 계정 여부 확인 (삭제되지 않고 잠금되지 않은 계정)
     */
    public boolean isActive() {
        return !isDeleted() && !isLocked();
    }
}

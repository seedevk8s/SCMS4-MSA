package com.scms.user.domain.entity;

import com.scms.user.domain.enums.AccountStatus;
import com.scms.user.domain.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 외부 회원 엔티티
 * - 이메일로 로그인하는 외부 사용자
 * - 일반 회원가입 및 소셜 로그인(OAuth2) 지원
 */
@Entity
@Table(name = "external_users", indexes = {
    @Index(name = "idx_external_user_email", columnList = "email"),
    @Index(name = "idx_external_user_provider", columnList = "provider, provider_id"),
    @Index(name = "idx_external_user_deleted_at", columnList = "deleted_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    /**
     * 이메일 (로그인 ID)
     */
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * 비밀번호 (암호화 저장, 소셜 로그인 시 null 가능)
     */
    @Size(max = 255, message = "비밀번호는 255자 이하여야 합니다")
    @Column(name = "password", length = 255)
    private String password;

    /**
     * 로그인 제공자 (LOCAL, GOOGLE, NAVER, KAKAO 등)
     */
    @NotBlank(message = "로그인 제공자는 필수입니다")
    @Size(max = 20, message = "로그인 제공자는 20자 이하여야 합니다")
    @Column(name = "provider", nullable = false, length = 20)
    @Builder.Default
    private String provider = "LOCAL";

    /**
     * 제공자별 사용자 ID (소셜 로그인 시 사용)
     */
    @Size(max = 255, message = "제공자 ID는 255자 이하여야 합니다")
    @Column(name = "provider_id", length = 255)
    private String providerId;

    /**
     * 프로필 이미지 URL (소셜 로그인 시 제공)
     */
    @Size(max = 500, message = "프로필 이미지 URL은 500자 이하여야 합니다")
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    /**
     * 이름
     */
    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다")
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * 전화번호
     */
    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다")
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 생년월일
     */
    @NotNull(message = "생년월일은 필수입니다")
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    /**
     * 주소
     */
    @Size(max = 200, message = "주소는 200자 이하여야 합니다")
    @Column(name = "address", length = 200)
    private String address;

    /**
     * 성별
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    /**
     * 계정 상태 (ACTIVE, INACTIVE, SUSPENDED)
     */
    @NotNull(message = "계정 상태는 필수입니다")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

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
     * 이메일 인증 여부
     */
    @NotNull
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    /**
     * 이메일 인증 토큰
     */
    @Size(max = 255, message = "이메일 인증 토큰은 255자 이하여야 합니다")
    @Column(name = "email_verify_token", length = 255)
    private String emailVerifyToken;

    /**
     * 이메일 인증 완료 일시
     */
    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    /**
     * 이용약관 동의 여부
     */
    @NotNull
    @Column(name = "agree_terms", nullable = false)
    @Builder.Default
    private Boolean agreeTerms = false;

    /**
     * 개인정보처리방침 동의 여부
     */
    @NotNull
    @Column(name = "agree_privacy", nullable = false)
    @Builder.Default
    private Boolean agreePrivacy = false;

    /**
     * 마케팅 정보 수신 동의 여부
     */
    @Column(name = "agree_marketing")
    @Builder.Default
    private Boolean agreeMarketing = false;

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

    /**
     * 마지막 로그인 일시
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

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
     * 이메일 인증 처리
     */
    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerifiedAt = LocalDateTime.now();
        this.emailVerifyToken = null;
    }

    /**
     * 이메일 인증 토큰 업데이트
     */
    public void updateEmailVerifyToken(String token) {
        this.emailVerifyToken = token;
    }

    /**
     * 마지막 로그인 시간 업데이트
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Soft Delete
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 소셜 로그인 사용자 여부 확인
     */
    public boolean isSocialUser() {
        return !"LOCAL".equals(this.provider);
    }

    /**
     * 소셜 로그인 정보 업데이트
     */
    public void updateSocialInfo(String name, String profileImageUrl) {
        this.name = name;
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            this.profileImageUrl = profileImageUrl;
        }
    }

    /**
     * 계정 잠금 여부 확인
     */
    public boolean isLocked() {
        return this.locked != null && this.locked;
    }

    /**
     * 활성 계정 여부 확인
     */
    public boolean isActive() {
        return !isDeleted() && !isLocked() && AccountStatus.ACTIVE.equals(this.status);
    }
}

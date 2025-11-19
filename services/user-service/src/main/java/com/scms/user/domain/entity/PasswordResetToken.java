package com.scms.user.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 비밀번호 재설정 토큰 엔티티
 * - 이메일을 통한 안전한 비밀번호 재설정을 위한 토큰 관리
 * - 내부 회원(User)과 외부 회원(ExternalUser) 모두 지원
 * - 일회용 토큰으로 보안 강화
 */
@Entity
@Table(name = "password_reset_tokens", indexes = {
    @Index(name = "idx_password_reset_token", columnList = "token"),
    @Index(name = "idx_password_reset_email", columnList = "email"),
    @Index(name = "idx_password_reset_expires_at", columnList = "expires_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Integer tokenId;

    /**
     * 토큰 문자열 (UUID 또는 암호화된 문자열)
     * - 이메일로 전송되는 고유 토큰
     * - URL 파라미터로 사용
     */
    @NotBlank(message = "토큰은 필수입니다")
    @Size(max = 255, message = "토큰은 255자 이하여야 합니다")
    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    /**
     * 토큰 타입 (INTERNAL: 내부회원, EXTERNAL: 외부회원)
     */
    @NotNull(message = "토큰 타입은 필수입니다")
    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false, length = 20)
    private TokenType tokenType;

    /**
     * 내부 회원 참조 (내부회원인 경우)
     * - User 테이블과 다대일 관계
     * - tokenType이 INTERNAL인 경우 필수
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 외부 회원 참조 (외부회원인 경우)
     * - ExternalUser 테이블과 다대일 관계
     * - tokenType이 EXTERNAL인 경우 필수
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "external_user_id")
    private ExternalUser externalUser;

    /**
     * 이메일 (추가 확인용)
     * - 토큰 검증 시 이메일도 함께 확인하여 보안 강화
     */
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다")
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    /**
     * 토큰 생성 시간
     */
    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 토큰 만료 시간
     * - 기본값: 생성 시간 + 1시간
     * - 보안을 위해 짧은 유효 시간 설정
     */
    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * 토큰 사용 여부
     * - 일회용 토큰이므로 사용 후 재사용 불가
     */
    @NotNull
    @Column(name = "used", nullable = false)
    @Builder.Default
    private Boolean used = false;

    /**
     * 토큰 사용 시간
     * - 비밀번호 재설정 완료 시간
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        // 기본 만료 시간: 1시간
        if (this.expiresAt == null) {
            this.expiresAt = this.createdAt.plusHours(1);
        }
    }

    /**
     * 토큰 만료 여부 확인
     *
     * @return 만료되었으면 true, 아니면 false
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * 토큰 사용 가능 여부 확인
     * - 사용되지 않았고 만료되지 않은 경우에만 유효
     *
     * @return 사용 가능하면 true, 아니면 false
     */
    public boolean isValid() {
        return !this.used && !isExpired();
    }

    /**
     * 토큘 사용 처리
     * - 비밀번호 재설정 완료 시 호출
     * - 재사용 방지를 위해 used 플래그 설정
     */
    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * 내부 회원용 토큰 생성 (정적 팩토리 메서드)
     */
    public static PasswordResetToken createForUser(String token, User user, String email) {
        return PasswordResetToken.builder()
                .token(token)
                .tokenType(TokenType.INTERNAL)
                .user(user)
                .email(email)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();
    }

    /**
     * 외부 회원용 토큰 생성 (정적 팩토리 메서드)
     */
    public static PasswordResetToken createForExternalUser(String token, ExternalUser externalUser, String email) {
        return PasswordResetToken.builder()
                .token(token)
                .tokenType(TokenType.EXTERNAL)
                .externalUser(externalUser)
                .email(email)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();
    }

    /**
     * 토큰 타입 Enum
     * - 내부 회원과 외부 회원을 구분
     */
    public enum TokenType {
        /**
         * 내부 회원 (학생, 상담사, 관리자)
         * - users 테이블
         */
        INTERNAL("내부회원"),

        /**
         * 외부 회원
         * - external_users 테이블
         */
        EXTERNAL("외부회원");

        private final String description;

        TokenType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}

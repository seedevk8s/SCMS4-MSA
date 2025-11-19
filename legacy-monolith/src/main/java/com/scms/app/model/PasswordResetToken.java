package com.scms.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 비밀번호 재설정 토큰 엔티티
 * - 이메일을 통한 안전한 비밀번호 재설정을 위한 토큰 관리
 */
@Entity
@Table(name = "password_reset_tokens")
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
     * 토큰 문자열 (UUID)
     */
    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    /**
     * 토큰 타입 (INTERNAL: 내부회원, EXTERNAL: 외부회원)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false, length = 20)
    private TokenType tokenType;

    /**
     * 내부 회원 참조 (내부회원인 경우)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 외부 회원 참조 (외부회원인 경우)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "external_user_id")
    private ExternalUser externalUser;

    /**
     * 이메일 (추가 확인용)
     */
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    /**
     * 토큰 생성 시간
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 토큰 만료 시간 (기본 1시간)
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * 토큰 사용 여부
     */
    @Column(name = "used", nullable = false)
    @Builder.Default
    private Boolean used = false;

    /**
     * 토큰 사용 시간
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
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * 토큰 사용 가능 여부 확인
     */
    public boolean isValid() {
        return !this.used && !isExpired();
    }

    /**
     * 토큰 사용 처리
     */
    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * 토큰 타입
     */
    public enum TokenType {
        INTERNAL,  // 내부회원 (학생)
        EXTERNAL   // 외부회원
    }
}

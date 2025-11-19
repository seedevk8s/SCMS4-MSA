package com.scms.user.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 로그인 이력 엔티티
 * - 사용자의 로그인 시도 기록 (성공/실패)
 * - 보안 감사 및 통계 분석에 활용
 */
@Entity
@Table(name = "login_history", indexes = {
    @Index(name = "idx_login_history_user_id", columnList = "user_id"),
    @Index(name = "idx_login_history_login_at", columnList = "login_at"),
    @Index(name = "idx_login_history_success", columnList = "is_success")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Integer historyId;

    /**
     * 로그인 시도한 사용자 (User 테이블 참조)
     * - LAZY 로딩으로 성능 최적화
     * - 다대일 관계: 한 사용자는 여러 로그인 이력을 가질 수 있음
     */
    @NotNull(message = "사용자는 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 로그인 시도 일시
     */
    @Column(name = "login_at", nullable = false)
    private LocalDateTime loginAt;

    /**
     * 로그인 시도 IP 주소
     * - IPv4: xxx.xxx.xxx.xxx (최대 15자)
     * - IPv6: xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx (최대 45자)
     */
    @Size(max = 45, message = "IP 주소는 45자 이하여야 합니다")
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * 사용자 에이전트 (브라우저, OS 정보)
     */
    @Size(max = 255, message = "사용자 에이전트는 255자 이하여야 합니다")
    @Column(name = "user_agent", length = 255)
    private String userAgent;

    /**
     * 로그인 성공 여부
     * - true: 로그인 성공
     * - false: 로그인 실패
     */
    @NotNull(message = "로그인 성공 여부는 필수입니다")
    @Column(name = "is_success", nullable = false)
    private Boolean isSuccess;

    /**
     * 로그인 실패 사유
     * - 예: "잘못된 비밀번호", "계정 잠김", "존재하지 않는 계정" 등
     */
    @Size(max = 255, message = "실패 사유는 255자 이하여야 합니다")
    @Column(name = "fail_reason", length = 255)
    private String failReason;

    @PrePersist
    protected void onCreate() {
        if (this.loginAt == null) {
            this.loginAt = LocalDateTime.now();
        }
    }

    /**
     * 로그인 성공 기록 생성 (정적 팩토리 메서드)
     */
    public static LoginHistory createSuccessHistory(User user, String ipAddress, String userAgent) {
        return LoginHistory.builder()
                .user(user)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .isSuccess(true)
                .loginAt(LocalDateTime.now())
                .build();
    }

    /**
     * 로그인 실패 기록 생성 (정적 팩토리 메서드)
     */
    public static LoginHistory createFailHistory(User user, String ipAddress, String userAgent, String failReason) {
        return LoginHistory.builder()
                .user(user)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .isSuccess(false)
                .failReason(failReason)
                .loginAt(LocalDateTime.now())
                .build();
    }
}

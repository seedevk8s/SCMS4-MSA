package com.scms.notification.domain.entity;

import com.scms.notification.domain.enums.NotificationPriority;
import com.scms.notification.domain.enums.NotificationStatus;
import com.scms.notification.domain.enums.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 알림 Entity
 *
 * 사용자에게 전송되는 모든 알림을 관리합니다.
 *
 * 주요 기능:
 * - 시스템 알림 (인앱)
 * - 이메일 알림
 * - 읽음/안읽음 상태 관리
 * - 우선순위별 알림
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at"),
        @Index(name = "idx_user_status", columnList = "user_id,status")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    /**
     * 수신자 User ID (User Service의 userId)
     * Foreign Key 없음 (MSA Database Per Service 패턴)
     */
    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 알림 제목
     */
    @NotBlank
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 알림 내용
     */
    @NotBlank
    @Size(max = 2000)
    @Column(name = "content", nullable = false, length = 2000, columnDefinition = "TEXT")
    private String content;

    /**
     * 알림 유형
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationType type;

    /**
     * 알림 상태
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.UNREAD;

    /**
     * 우선순위
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "priority", nullable = false, length = 20)
    private NotificationPriority priority = NotificationPriority.NORMAL;

    /**
     * 관련 엔티티 유형 (PROGRAM, CONSULTATION, USER 등)
     */
    @Size(max = 50)
    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;

    /**
     * 관련 엔티티 ID
     */
    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    /**
     * 링크 URL (클릭 시 이동할 페이지)
     */
    @Size(max = 500)
    @Column(name = "link_url", length = 500)
    private String linkUrl;

    /**
     * 이메일 발송 여부
     */
    @Builder.Default
    @Column(name = "email_sent", nullable = false)
    private Boolean emailSent = false;

    /**
     * 이메일 발송 시각
     */
    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    /**
     * 읽음 처리 시각
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * 만료 일시 (특정 기간 후 자동 삭제)
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * 생성 일시 (자동 설정)
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 템플릿 ID (NotificationTemplate과 연결)
     */
    @Column(name = "template_id")
    private Long templateId;

    /**
     * 추가 메타데이터 (JSON)
     */
    @Column(name = "metadata", length = 1000, columnDefinition = "TEXT")
    private String metadata;

    // ==================== Business Methods ====================

    /**
     * 읽음 처리
     */
    public void markAsRead() {
        this.status = NotificationStatus.READ;
        this.readAt = LocalDateTime.now();
    }

    /**
     * 삭제 처리 (Soft Delete)
     */
    public void markAsDeleted() {
        this.status = NotificationStatus.DELETED;
    }

    /**
     * 보관 처리
     */
    public void markAsArchived() {
        this.status = NotificationStatus.ARCHIVED;
    }

    /**
     * 이메일 발송 완료 처리
     */
    public void markEmailSent() {
        this.emailSent = true;
        this.emailSentAt = LocalDateTime.now();
    }

    /**
     * 읽음 여부 확인
     */
    public boolean isRead() {
        return this.status == NotificationStatus.READ;
    }

    /**
     * 만료 여부 확인
     */
    public boolean isExpired() {
        return this.expiresAt != null && this.expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * 긴급 알림 여부
     */
    public boolean isUrgent() {
        return this.priority == NotificationPriority.URGENT;
    }
}

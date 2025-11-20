package com.scms.notification.dto.response;

import com.scms.notification.domain.entity.Notification;
import com.scms.notification.domain.enums.NotificationPriority;
import com.scms.notification.domain.enums.NotificationStatus;
import com.scms.notification.domain.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알림 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long notificationId;
    private Long userId;
    private String title;
    private String content;
    private NotificationType type;
    private NotificationStatus status;
    private NotificationPriority priority;
    private String relatedEntityType;
    private Long relatedEntityId;
    private String linkUrl;
    private Boolean emailSent;
    private LocalDateTime emailSentAt;
    private LocalDateTime readAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private Long templateId;
    private String metadata;

    /**
     * Entity → Response DTO 변환
     */
    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .status(notification.getStatus())
                .priority(notification.getPriority())
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                .linkUrl(notification.getLinkUrl())
                .emailSent(notification.getEmailSent())
                .emailSentAt(notification.getEmailSentAt())
                .readAt(notification.getReadAt())
                .expiresAt(notification.getExpiresAt())
                .createdAt(notification.getCreatedAt())
                .templateId(notification.getTemplateId())
                .metadata(notification.getMetadata())
                .build();
    }
}

package com.scms.notification.dto.request;

import com.scms.notification.domain.enums.NotificationPriority;
import com.scms.notification.domain.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알림 생성 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreateRequest {

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 최대 200자입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    @Size(max = 2000, message = "내용은 최대 2000자입니다.")
    private String content;

    @NotNull(message = "알림 유형은 필수입니다.")
    private NotificationType type;

    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;

    private String relatedEntityType;

    private Long relatedEntityId;

    private String linkUrl;

    private LocalDateTime expiresAt;

    private Long templateId;

    private String metadata;
}

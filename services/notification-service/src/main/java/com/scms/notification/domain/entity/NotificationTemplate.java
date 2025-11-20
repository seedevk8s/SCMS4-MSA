package com.scms.notification.domain.entity;

import com.scms.notification.domain.enums.NotificationPriority;
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
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 알림 템플릿 Entity
 *
 * 재사용 가능한 알림 템플릿을 관리합니다.
 *
 * 주요 기능:
 * - 템플릿 기반 알림 생성
 * - 변수 치환 ({{userName}}, {{programName}} 등)
 * - 이메일 제목/본문 템플릿
 */
@Entity
@Table(name = "notification_templates", indexes = {
        @Index(name = "idx_template_code", columnList = "template_code", unique = true),
        @Index(name = "idx_type", columnList = "type")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    /**
     * 템플릿 코드 (unique, 예: USER_CREATED, PROGRAM_APPROVED)
     */
    @NotBlank
    @Size(max = 50)
    @Column(name = "template_code", nullable = false, unique = true, length = 50)
    private String templateCode;

    /**
     * 템플릿 이름
     */
    @NotBlank
    @Size(max = 100)
    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;

    /**
     * 템플릿 설명
     */
    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 알림 유형
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationType type;

    /**
     * 우선순위
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "priority", nullable = false, length = 20)
    private NotificationPriority priority = NotificationPriority.NORMAL;

    /**
     * 알림 제목 템플릿
     * 변수 사용 가능: {{userName}}, {{programName}}, {{date}} 등
     */
    @NotBlank
    @Size(max = 200)
    @Column(name = "title_template", nullable = false, length = 200)
    private String titleTemplate;

    /**
     * 알림 내용 템플릿
     * 변수 사용 가능: {{userName}}, {{programName}}, {{date}} 등
     */
    @NotBlank
    @Size(max = 2000)
    @Column(name = "content_template", nullable = false, length = 2000, columnDefinition = "TEXT")
    private String contentTemplate;

    /**
     * 이메일 제목 템플릿 (이메일 발송 시)
     */
    @Size(max = 200)
    @Column(name = "email_subject_template", length = 200)
    private String emailSubjectTemplate;

    /**
     * 이메일 본문 템플릿 (이메일 발송 시)
     */
    @Size(max = 5000)
    @Column(name = "email_body_template", length = 5000, columnDefinition = "TEXT")
    private String emailBodyTemplate;

    /**
     * 링크 URL 템플릿
     * 예: /programs/{{programId}}
     */
    @Size(max = 500)
    @Column(name = "link_url_template", length = 500)
    private String linkUrlTemplate;

    /**
     * 활성화 여부
     */
    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

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
     * 생성자 ID (관리자)
     */
    @Column(name = "created_by")
    private Long createdBy;

    // ==================== Business Methods ====================

    /**
     * 변수 치환하여 제목 생성
     *
     * @param variables 변수 맵 (예: {"userName": "홍길동", "programName": "Java 특강"})
     * @return 치환된 제목
     */
    public String renderTitle(java.util.Map<String, String> variables) {
        String result = titleTemplate;
        for (java.util.Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    /**
     * 변수 치환하여 내용 생성
     *
     * @param variables 변수 맵
     * @return 치환된 내용
     */
    public String renderContent(java.util.Map<String, String> variables) {
        String result = contentTemplate;
        for (java.util.Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    /**
     * 변수 치환하여 링크 URL 생성
     *
     * @param variables 변수 맵
     * @return 치환된 링크 URL
     */
    public String renderLinkUrl(java.util.Map<String, String> variables) {
        if (linkUrlTemplate == null) {
            return null;
        }
        String result = linkUrlTemplate;
        for (java.util.Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    /**
     * 활성화
     */
    public void activate() {
        this.active = true;
    }

    /**
     * 비활성화
     */
    public void deactivate() {
        this.active = false;
    }
}

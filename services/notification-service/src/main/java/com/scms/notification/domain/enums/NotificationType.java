package com.scms.notification.domain.enums;

/**
 * 알림 유형
 */
public enum NotificationType {
    /**
     * 시스템 알림 (인앱 알림만)
     */
    SYSTEM,

    /**
     * 이메일 알림
     */
    EMAIL,

    /**
     * SMS 알림 (향후 구현)
     */
    SMS,

    /**
     * 푸시 알림 (향후 구현)
     */
    PUSH,

    /**
     * 시스템 + 이메일 (복합)
     */
    SYSTEM_EMAIL,

    /**
     * 모든 채널 (시스템 + 이메일 + SMS + 푸시)
     */
    ALL
}

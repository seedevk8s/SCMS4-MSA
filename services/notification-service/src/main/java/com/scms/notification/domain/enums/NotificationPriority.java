package com.scms.notification.domain.enums;

/**
 * 알림 우선순위
 */
public enum NotificationPriority {
    /**
     * 낮음 (일반 안내, 광고)
     */
    LOW,

    /**
     * 보통 (일반 알림)
     */
    NORMAL,

    /**
     * 높음 (중요 알림 - 프로그램 승인, 마감 임박)
     */
    HIGH,

    /**
     * 긴급 (즉시 확인 필요 - 계정 잠금, 시스템 점검)
     */
    URGENT
}

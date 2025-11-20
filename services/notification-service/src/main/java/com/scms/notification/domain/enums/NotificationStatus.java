package com.scms.notification.domain.enums;

/**
 * 알림 상태
 */
public enum NotificationStatus {
    /**
     * 읽지 않음 (기본값)
     */
    UNREAD,

    /**
     * 읽음
     */
    READ,

    /**
     * 삭제됨 (Soft Delete)
     */
    DELETED,

    /**
     * 보관됨 (중요 알림 보관)
     */
    ARCHIVED
}

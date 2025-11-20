package com.scms.program.domain.enums;

/**
 * 프로그램 신청 상태
 */
public enum ApplicationStatus {
    /**
     * 신청됨 (승인 대기)
     */
    PENDING,

    /**
     * 승인됨
     */
    APPROVED,

    /**
     * 거절됨
     */
    REJECTED,

    /**
     * 취소됨 (신청자가 취소)
     */
    CANCELLED,

    /**
     * 출석 완료
     */
    ATTENDED,

    /**
     * 수료 완료
     */
    COMPLETED
}

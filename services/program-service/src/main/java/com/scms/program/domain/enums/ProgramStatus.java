package com.scms.program.domain.enums;

/**
 * 프로그램 상태
 */
public enum ProgramStatus {
    /**
     * 작성중 (임시저장)
     */
    DRAFT,

    /**
     * 승인 대기중
     */
    PENDING,

    /**
     * 승인됨 (모집중)
     */
    APPROVED,

    /**
     * 모집 마감
     */
    CLOSED,

    /**
     * 진행중
     */
    IN_PROGRESS,

    /**
     * 완료됨
     */
    COMPLETED,

    /**
     * 취소됨
     */
    CANCELLED
}

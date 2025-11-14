package com.scms.app.model;

/**
 * 프로그램 신청 상태
 */
public enum ApplicationStatus {
    PENDING("대기"),           // 신청 대기
    APPROVED("승인"),          // 신청 승인
    REJECTED("거부"),          // 신청 거부
    CANCELLED("취소"),         // 신청 취소
    COMPLETED("참여완료");     // 프로그램 참여 완료

    private final String description;

    ApplicationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

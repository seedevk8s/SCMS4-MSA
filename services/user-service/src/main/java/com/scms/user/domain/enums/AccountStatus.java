package com.scms.user.domain.enums;

/**
 * 계정 상태 Enum
 * - 외부 회원의 계정 상태를 관리
 */
public enum AccountStatus {
    /**
     * 활성 - 정상적으로 사용 가능한 계정
     */
    ACTIVE("활성"),

    /**
     * 비활성 - 사용자가 비활성화한 계정
     */
    INACTIVE("비활성"),

    /**
     * 정지 - 관리자에 의해 정지된 계정
     */
    SUSPENDED("정지");

    private final String description;

    AccountStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

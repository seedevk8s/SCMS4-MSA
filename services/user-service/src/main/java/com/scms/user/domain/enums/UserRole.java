package com.scms.user.domain.enums;

/**
 * 사용자 역할 Enum
 * - 내부 회원(users 테이블)의 역할 구분
 */
public enum UserRole {
    /**
     * 학생
     */
    STUDENT("학생"),

    /**
     * 상담사
     */
    COUNSELOR("상담사"),

    /**
     * 관리자
     */
    ADMIN("관리자");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

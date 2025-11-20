package com.scms.portfolio.domain.enums;

/**
 * 공개 범위
 */
public enum VisibilityLevel {
    PUBLIC("전체 공개", "누구나 열람 가능"),
    PRIVATE("비공개", "본인만 열람 가능"),
    COMPANY_ONLY("기업만 공개", "채용 담당자에게만 공개"),
    SCHOOL_ONLY("학교 내 공개", "학교 구성원에게만 공개");

    private final String displayName;
    private final String description;

    VisibilityLevel(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}

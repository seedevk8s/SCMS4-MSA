package com.scms.portfolio.domain.enums;

/**
 * 포트폴리오 상태
 */
public enum PortfolioStatus {
    DRAFT("임시 저장", "작성 중인 상태"),
    PUBLISHED("공개", "공개된 상태"),
    ARCHIVED("보관", "보관된 상태");

    private final String displayName;
    private final String description;

    PortfolioStatus(String displayName, String description) {
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

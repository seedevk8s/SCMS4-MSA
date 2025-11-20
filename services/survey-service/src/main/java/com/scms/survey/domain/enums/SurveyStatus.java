package com.scms.survey.domain.enums;

/**
 * 설문 상태
 */
public enum SurveyStatus {
    DRAFT("임시 저장", "작성 중인 설문"),
    PUBLISHED("공개", "응답 가능한 설문"),
    CLOSED("마감", "응답 마감된 설문"),
    ARCHIVED("보관", "보관된 설문");

    private final String displayName;
    private final String description;

    SurveyStatus(String displayName, String description) {
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

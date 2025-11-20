package com.scms.survey.domain.enums;

/**
 * 질문 타입
 */
public enum QuestionType {
    SINGLE_CHOICE("객관식 (단일 선택)", "하나만 선택 가능"),
    MULTIPLE_CHOICE("객관식 (복수 선택)", "여러 개 선택 가능"),
    SHORT_ANSWER("단답형", "짧은 텍스트 응답"),
    LONG_ANSWER("서술형", "긴 텍스트 응답"),
    RATING("평점", "별점, 점수 등"),
    SCALE("척도", "1-5, 1-10 등 척도"),
    DATE("날짜", "날짜 선택"),
    FILE_UPLOAD("파일 첨부", "파일 업로드");

    private final String displayName;
    private final String description;

    QuestionType(String displayName, String description) {
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

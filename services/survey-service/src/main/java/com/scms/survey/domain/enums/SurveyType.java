package com.scms.survey.domain.enums;

/**
 * 설문 타입
 */
public enum SurveyType {
    SATISFACTION("만족도 조사", "프로그램, 서비스 만족도 조사"),
    NEEDS_ASSESSMENT("수요 조사", "프로그램 수요 파악"),
    EMPLOYMENT("취업 현황", "졸업생 취업 현황 조사"),
    CAREER("진로 조사", "학생 진로 희망 조사"),
    PROGRAM_EVALUATION("프로그램 평가", "프로그램 운영 평가"),
    EVENT_FEEDBACK("행사 피드백", "행사 참여 후기"),
    GENERAL("일반 설문", "일반적인 설문조사"),
    OTHER("기타", "기타 설문");

    private final String displayName;
    private final String description;

    SurveyType(String displayName, String description) {
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

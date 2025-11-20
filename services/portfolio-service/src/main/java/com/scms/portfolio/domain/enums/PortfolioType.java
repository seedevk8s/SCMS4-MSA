package com.scms.portfolio.domain.enums;

/**
 * 포트폴리오 타입
 */
public enum PortfolioType {
    PROJECT("프로젝트", "개인 또는 팀 프로젝트"),
    AWARD("수상 경력", "대회, 공모전 수상 내역"),
    CERTIFICATE("자격증", "자격증 및 인증서"),
    ACTIVITY("대외 활동", "동아리, 봉사활동, 인턴십 등"),
    SKILL("기술 스택", "보유 기술 및 스킬"),
    EDUCATION("교육", "학력 및 교육 이수"),
    EXPERIENCE("경력", "직무 경험"),
    PUBLICATION("논문/출판", "연구 논문 및 출판물"),
    PATENT("특허", "특허 및 지식재산권"),
    LANGUAGE("어학", "외국어 능력"),
    OTHER("기타", "기타 항목");

    private final String displayName;
    private final String description;

    PortfolioType(String displayName, String description) {
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

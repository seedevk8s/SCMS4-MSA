package com.scms.survey.dto.response;

import com.scms.survey.domain.entity.Survey;
import com.scms.survey.domain.enums.SurveyStatus;
import com.scms.survey.domain.enums.SurveyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 설문 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyResponse {

    private Long surveyId;
    private String title;
    private String description;
    private SurveyType type;
    private SurveyStatus status;
    private Long createdBy;
    private Boolean anonymous;
    private Boolean allowMultipleResponses;
    private Boolean allowEdit;
    private Boolean showResults;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long responseCount;
    private Long maxResponses;
    private String targetGroup;
    private Boolean availableForResponse;
    private List<QuestionResponse> questions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity -> Response DTO 변환
     */
    public static SurveyResponse from(Survey survey) {
        return SurveyResponse.builder()
                .surveyId(survey.getSurveyId())
                .title(survey.getTitle())
                .description(survey.getDescription())
                .type(survey.getType())
                .status(survey.getStatus())
                .createdBy(survey.getCreatedBy())
                .anonymous(survey.getAnonymous())
                .allowMultipleResponses(survey.getAllowMultipleResponses())
                .allowEdit(survey.getAllowEdit())
                .showResults(survey.getShowResults())
                .startDate(survey.getStartDate())
                .endDate(survey.getEndDate())
                .responseCount(survey.getResponseCount())
                .maxResponses(survey.getMaxResponses())
                .targetGroup(survey.getTargetGroup())
                .availableForResponse(survey.isAvailableForResponse())
                .questions(survey.getQuestions().stream()
                        .filter(q -> q.getDeletedAt() == null)
                        .map(QuestionResponse::from)
                        .collect(Collectors.toList()))
                .createdAt(survey.getCreatedAt())
                .updatedAt(survey.getUpdatedAt())
                .build();
    }

    /**
     * 간략한 정보만 포함 (질문 제외)
     */
    public static SurveyResponse fromWithoutQuestions(Survey survey) {
        return SurveyResponse.builder()
                .surveyId(survey.getSurveyId())
                .title(survey.getTitle())
                .description(survey.getDescription())
                .type(survey.getType())
                .status(survey.getStatus())
                .createdBy(survey.getCreatedBy())
                .anonymous(survey.getAnonymous())
                .startDate(survey.getStartDate())
                .endDate(survey.getEndDate())
                .responseCount(survey.getResponseCount())
                .maxResponses(survey.getMaxResponses())
                .targetGroup(survey.getTargetGroup())
                .availableForResponse(survey.isAvailableForResponse())
                .createdAt(survey.getCreatedAt())
                .updatedAt(survey.getUpdatedAt())
                .build();
    }
}

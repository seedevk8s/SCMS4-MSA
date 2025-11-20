package com.scms.survey.dto.response;

import com.scms.survey.domain.entity.Question;
import com.scms.survey.domain.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 질문 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {

    private Long questionId;
    private Long surveyId;
    private QuestionType type;
    private String content;
    private String description;
    private Integer displayOrder;
    private Boolean required;
    private List<QuestionOptionResponse> options;
    private Integer minValue;
    private Integer maxValue;
    private String minLabel;
    private String maxLabel;
    private Integer maxSelections;
    private Integer maxLength;
    private String allowedFileExtensions;
    private Long maxFileSize;
    private LocalDateTime createdAt;

    /**
     * Entity -> Response DTO 변환
     */
    public static QuestionResponse from(Question question) {
        return QuestionResponse.builder()
                .questionId(question.getQuestionId())
                .surveyId(question.getSurvey().getSurveyId())
                .type(question.getType())
                .content(question.getContent())
                .description(question.getDescription())
                .displayOrder(question.getDisplayOrder())
                .required(question.getRequired())
                .options(question.getOptions().stream()
                        .filter(o -> o.getDeletedAt() == null)
                        .map(QuestionOptionResponse::from)
                        .collect(Collectors.toList()))
                .minValue(question.getMinValue())
                .maxValue(question.getMaxValue())
                .minLabel(question.getMinLabel())
                .maxLabel(question.getMaxLabel())
                .maxSelections(question.getMaxSelections())
                .maxLength(question.getMaxLength())
                .allowedFileExtensions(question.getAllowedFileExtensions())
                .maxFileSize(question.getMaxFileSize())
                .createdAt(question.getCreatedAt())
                .build();
    }
}

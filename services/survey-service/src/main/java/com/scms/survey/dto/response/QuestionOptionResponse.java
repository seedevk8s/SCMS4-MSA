package com.scms.survey.dto.response;

import com.scms.survey.domain.entity.QuestionOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 질문 선택지 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOptionResponse {

    private Long optionId;
    private Long questionId;
    private String content;
    private Integer displayOrder;
    private Boolean allowOtherInput;
    private Long selectionCount;

    /**
     * Entity -> Response DTO 변환
     */
    public static QuestionOptionResponse from(QuestionOption option) {
        return QuestionOptionResponse.builder()
                .optionId(option.getOptionId())
                .questionId(option.getQuestion().getQuestionId())
                .content(option.getContent())
                .displayOrder(option.getDisplayOrder())
                .allowOtherInput(option.getAllowOtherInput())
                .selectionCount(option.getSelectionCount())
                .build();
    }
}

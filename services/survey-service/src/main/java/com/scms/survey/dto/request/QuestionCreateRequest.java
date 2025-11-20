package com.scms.survey.dto.request;

import com.scms.survey.domain.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 질문 생성 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCreateRequest {

    @NotNull(message = "질문 타입은 필수입니다.")
    private QuestionType type;

    @NotBlank(message = "질문 내용은 필수입니다.")
    @Size(max = 5000)
    private String content;

    @Size(max = 1000)
    private String description;

    @Builder.Default
    private Integer displayOrder = 0;

    @Builder.Default
    private Boolean required = false;

    // 객관식인 경우
    private List<OptionCreateRequest> options;

    // 평점/척도인 경우
    private Integer minValue;
    private Integer maxValue;
    private String minLabel;
    private String maxLabel;

    // 복수 선택인 경우
    private Integer maxSelections;

    // 텍스트 응답인 경우
    private Integer maxLength;

    // 파일 첨부인 경우
    private String allowedFileExtensions;
    private Long maxFileSize;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionCreateRequest {
        @NotBlank
        @Size(max = 500)
        private String content;

        @Builder.Default
        private Integer displayOrder = 0;

        @Builder.Default
        private Boolean allowOtherInput = false;
    }
}

package com.scms.survey.dto.request;

import com.scms.survey.domain.enums.SurveyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 설문 생성 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200)
    private String title;

    @Size(max = 5000)
    private String description;

    @NotNull(message = "설문 타입은 필수입니다.")
    private SurveyType type;

    @Builder.Default
    private Boolean anonymous = false;

    @Builder.Default
    private Boolean allowMultipleResponses = false;

    @Builder.Default
    private Boolean allowEdit = true;

    @Builder.Default
    private Boolean showResults = false;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Size(max = 100)
    private String targetGroup;

    private Long maxResponses;
}

package com.scms.survey.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 설문 응답 제출 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyResponseSubmitRequest {

    @NotNull(message = "응답 목록은 필수입니다.")
    private List<Answer> answers;

    private String sessionId;  // 익명 응답용

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Answer {
        @NotNull
        private Long questionId;

        // 객관식 응답
        private List<Long> selectedOptionIds;

        // 텍스트 응답
        private String textAnswer;

        // 숫자 응답 (평점, 척도)
        private Integer numberAnswer;

        // 날짜 응답
        private LocalDateTime dateAnswer;

        // 파일 응답
        private String fileUrl;
        private String fileName;
    }
}

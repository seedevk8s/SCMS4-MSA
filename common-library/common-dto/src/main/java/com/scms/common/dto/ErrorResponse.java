package com.scms.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 에러 응답 DTO
 * 예외 발생 시 클라이언트에게 전달되는 에러 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * 에러 코드
     */
    private String errorCode;

    /**
     * 에러 메시지
     */
    private String message;

    /**
     * 에러 상세 정보
     */
    private String details;

    /**
     * 필드별 에러 정보 (유효성 검증 실패 시)
     */
    private List<FieldError> fieldErrors;

    /**
     * 발생 시간
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 요청 경로
     */
    private String path;

    /**
     * 필드별 에러 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private Object rejectedValue;
        private String message;
    }
}

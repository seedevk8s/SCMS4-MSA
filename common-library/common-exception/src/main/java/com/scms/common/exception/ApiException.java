package com.scms.common.exception;

import lombok.Getter;

/**
 * API 예외 클래스
 *
 * ErrorCode를 사용하여 예외를 생성합니다.
 */
@Getter
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String customMessage;

    /**
     * ErrorCode만 사용하는 생성자
     */
    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
    }

    /**
     * ErrorCode와 커스텀 메시지를 사용하는 생성자
     */
    public ApiException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }

    /**
     * ErrorCode, 커스텀 메시지, 원인 예외를 사용하는 생성자
     */
    public ApiException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }

    /**
     * 실제로 표시할 메시지 반환
     */
    public String getDisplayMessage() {
        return customMessage != null ? customMessage : errorCode.getMessage();
    }
}

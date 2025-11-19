package com.scms.common.exception;

import lombok.Getter;

/**
 * 기본 예외 클래스
 * 모든 커스텀 예외의 부모 클래스
 */
@Getter
public class BaseException extends RuntimeException {

    private final String errorCode;

    public BaseException(String message) {
        super(message);
        this.errorCode = "INTERNAL_ERROR";
    }

    public BaseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BaseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}

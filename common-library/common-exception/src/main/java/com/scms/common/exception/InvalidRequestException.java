package com.scms.common.exception;

/**
 * 잘못된 요청일 때 발생하는 예외
 */
public class InvalidRequestException extends BaseException {

    public InvalidRequestException(String message) {
        super("INVALID_REQUEST", message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super("INVALID_REQUEST", message, cause);
    }
}

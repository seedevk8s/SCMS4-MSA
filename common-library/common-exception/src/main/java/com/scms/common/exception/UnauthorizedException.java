package com.scms.common.exception;

/**
 * 인증되지 않은 요청일 때 발생하는 예외
 */
public class UnauthorizedException extends BaseException {

    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }

    public UnauthorizedException() {
        super("UNAUTHORIZED", "인증되지 않은 요청입니다.");
    }
}

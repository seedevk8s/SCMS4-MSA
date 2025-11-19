package com.scms.common.exception;

/**
 * 권한이 없을 때 발생하는 예외
 */
public class ForbiddenException extends BaseException {

    public ForbiddenException(String message) {
        super("FORBIDDEN", message);
    }

    public ForbiddenException() {
        super("FORBIDDEN", "접근 권한이 없습니다.");
    }
}

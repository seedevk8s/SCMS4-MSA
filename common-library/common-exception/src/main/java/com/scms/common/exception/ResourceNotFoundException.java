package com.scms.common.exception;

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외
 */
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super("NOT_FOUND", String.format("%s을(를) 찾을 수 없습니다: %s=%s", resourceName, fieldName, fieldValue));
    }

    public ResourceNotFoundException(String message) {
        super("NOT_FOUND", message);
    }
}

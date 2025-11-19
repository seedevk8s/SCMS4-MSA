package com.scms.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 *
 * 각 에러 코드는 HTTP 상태 코드와 메시지를 포함합니다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통 에러 (1000번대)
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "E1000", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "E1001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "E1002", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "E1003", "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E1004", "서버 내부 오류가 발생했습니다."),

    // 사용자 관련 에러 (2000번대)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "E2000", "사용자를 찾을 수 없습니다."),
    USER_DELETED(HttpStatus.GONE, "E2001", "삭제된 사용자입니다."),
    DUPLICATE_STUDENT_NUM(HttpStatus.CONFLICT, "E2002", "이미 사용 중인 학번입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "E2003", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "E2004", "비밀번호가 일치하지 않습니다."),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "E2005", "계정이 잠겼습니다."),
    ACCOUNT_INACTIVE(HttpStatus.FORBIDDEN, "E2006", "비활성화된 계정입니다."),
    ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN, "E2007", "정지된 계정입니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "E2008", "이메일 인증이 필요합니다."),

    // 학생 관련 에러 (2100번대)
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "E2100", "학생 정보를 찾을 수 없습니다."),

    // 상담사 관련 에러 (2200번대)
    COUNSELOR_NOT_FOUND(HttpStatus.NOT_FOUND, "E2200", "상담사 정보를 찾을 수 없습니다."),

    // 인증 관련 에러 (3000번대)
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "E3000", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "E3001", "만료된 토큰입니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "E3002", "유효하지 않은 인증 코드입니다."),

    // 프로그램 관련 에러 (4000번대)
    PROGRAM_NOT_FOUND(HttpStatus.NOT_FOUND, "E4000", "프로그램을 찾을 수 없습니다."),
    PROGRAM_APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "E4001", "프로그램 신청을 찾을 수 없습니다."),
    PROGRAM_FULL(HttpStatus.CONFLICT, "E4002", "프로그램 정원이 초과되었습니다."),
    DUPLICATE_APPLICATION(HttpStatus.CONFLICT, "E4003", "이미 신청한 프로그램입니다."),

    // 포트폴리오 관련 에러 (5000번대)
    PORTFOLIO_NOT_FOUND(HttpStatus.NOT_FOUND, "E5000", "포트폴리오를 찾을 수 없습니다."),
    PORTFOLIO_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "E5001", "포트폴리오 항목을 찾을 수 없습니다."),

    // 설문 관련 에러 (6000번대)
    SURVEY_NOT_FOUND(HttpStatus.NOT_FOUND, "E6000", "설문을 찾을 수 없습니다."),
    SURVEY_ALREADY_SUBMITTED(HttpStatus.CONFLICT, "E6001", "이미 제출한 설문입니다."),
    SURVEY_CLOSED(HttpStatus.FORBIDDEN, "E6002", "설문 응답 기간이 아닙니다."),

    // 상담 관련 에러 (7000번대)
    CONSULTATION_NOT_FOUND(HttpStatus.NOT_FOUND, "E7000", "상담을 찾을 수 없습니다."),
    CONSULTATION_SLOT_UNAVAILABLE(HttpStatus.CONFLICT, "E7001", "선택한 시간대는 예약할 수 없습니다."),

    // 알림 관련 에러 (8000번대)
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "E8000", "알림을 찾을 수 없습니다."),

    // 파일 관련 에러 (9000번대)
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E9000", "파일 업로드에 실패했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "E9001", "파일을 찾을 수 없습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "E9002", "허용되지 않는 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "E9003", "파일 크기가 제한을 초과했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

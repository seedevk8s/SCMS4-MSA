package com.scms.common.util;

/**
 * 문자열 관련 유틸리티 클래스
 */
public class StringUtils {

    /**
     * 문자열이 null이거나 비어있는지 확인
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 문자열이 null이 아니고 비어있지 않은지 확인
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 문자열을 마스킹 처리 (예: 이메일, 전화번호)
     */
    public static String mask(String str, int visibleChars) {
        if (isEmpty(str) || str.length() <= visibleChars) {
            return str;
        }

        String visible = str.substring(0, visibleChars);
        String masked = "*".repeat(str.length() - visibleChars);
        return visible + masked;
    }

    /**
     * 이메일 마스킹 (예: abc@example.com -> a**@example.com)
     */
    public static String maskEmail(String email) {
        if (isEmpty(email) || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        if (parts[0].length() <= 1) {
            return email;
        }

        String maskedLocal = parts[0].charAt(0) + "**";
        return maskedLocal + "@" + parts[1];
    }

    /**
     * 전화번호 마스킹 (예: 010-1234-5678 -> 010-****-5678)
     */
    public static String maskPhone(String phone) {
        if (isEmpty(phone)) {
            return phone;
        }

        // 전화번호에서 숫자만 추출
        String digits = phone.replaceAll("[^0-9]", "");

        if (digits.length() < 7) {
            return phone;
        }

        // 앞 3자리와 뒤 4자리만 보이도록
        String prefix = digits.substring(0, 3);
        String suffix = digits.substring(digits.length() - 4);
        String middle = "*".repeat(digits.length() - 7);

        return prefix + "-" + middle + "-" + suffix;
    }

    /**
     * 문자열 자르기 (최대 길이 제한)
     */
    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }

        return str.substring(0, maxLength) + "...";
    }

    /**
     * null인 경우 기본값 반환
     */
    public static String defaultIfNull(String str, String defaultValue) {
        return str == null ? defaultValue : str;
    }

    /**
     * 비어있는 경우 기본값 반환
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }
}

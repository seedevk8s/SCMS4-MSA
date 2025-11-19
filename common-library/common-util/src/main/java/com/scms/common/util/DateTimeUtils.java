package com.scms.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 날짜/시간 관련 유틸리티 클래스
 */
public class DateTimeUtils {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATETIME_FORMAT_WITH_MILLIS = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * LocalDateTime을 문자열로 변환
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
    }

    /**
     * LocalDate를 문자열로 변환
     */
    public static String format(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    /**
     * 문자열을 LocalDateTime으로 변환
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(DATETIME_FORMAT));
    }

    /**
     * 문자열을 LocalDate로 변환
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    /**
     * 두 날짜 사이의 일수 계산
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * 두 날짜/시간 사이의 시간 차이 계산 (초 단위)
     */
    public static long secondsBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.SECONDS.between(start, end);
    }

    /**
     * 현재 날짜가 주어진 범위 내에 있는지 확인
     */
    public static boolean isWithinRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * 현재 날짜/시간이 주어진 범위 내에 있는지 확인
     */
    public static boolean isWithinRange(LocalDateTime dateTime, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return !dateTime.isBefore(startDateTime) && !dateTime.isAfter(endDateTime);
    }
}

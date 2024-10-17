package com.example.chatservermessage.domain.utility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    // ISO 8601 포맷 패턴 (예: 2023-10-17T14:30:00)
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // LocalDateTime을 String으로 변환하는 메소드
    public static String toStringTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.format(FORMATTER);  // LocalDateTime -> String
    }

    // String을 LocalDateTime으로 변환하는 메소드
    public static LocalDateTime toLocalDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeString, FORMATTER);  // String -> LocalDateTime
    }
}


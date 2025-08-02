package ru.practicum.config;

import java.time.format.DateTimeFormatter;

public class DateConfig {
    public static final String FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(FORMAT);
}

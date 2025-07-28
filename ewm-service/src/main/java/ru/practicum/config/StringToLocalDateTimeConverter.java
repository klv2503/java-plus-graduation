package ru.practicum.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Component
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

    @Override
    public LocalDateTime convert(String source) {
        if (source.isBlank()) {
            return null;
        }

        try {
            String decodedDate = URLDecoder.decode(source, StandardCharsets.UTF_8);
            return LocalDateTime.parse(decodedDate, DateConfig.FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + source, e);
        }
    }
}

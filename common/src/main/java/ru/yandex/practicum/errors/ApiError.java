package ru.yandex.practicum.errors;

import lombok.Data;
import ru.yandex.practicum.config.DateConfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class ApiError {
    List<String> errors;
    String message;
    String reason;
    String status;
    String timestamp;

    public ApiError(String status, String reason, Throwable ex) {
        this.errors = acceptStackTrace(ex);
        this.message = ex.getMessage();
        this.reason = reason;
        this.status = status;
        this.timestamp = LocalDateTime.now().format(DateConfig.FORMATTER);
    }

    private List<String> acceptStackTrace(Throwable ex) {
        return Stream.of(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.toList());
    }
}

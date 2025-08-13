package ru.yandex.practicum.validation;

import jakarta.validation.ConstraintViolationException;
import ru.yandex.practicum.config.DateConfig;
import ru.yandex.practicum.dto.SearchEventsParams;

import java.time.LocalDateTime;

public class SearchParamsValidator {

    public static void validateSearchParams(SearchEventsParams se) throws ConstraintViolationException {
        String dateErrorMessage = "RangeStart not appointed but rangeEnd is earlier as now or " +
                "Date of rangeEnd is earlier as date of rangeStart";
        Long minCatId = se.getCategories().stream()
                .min(Long::compareTo)
                .orElse(null);
        if (minCatId != null && minCatId < 1L) {
            throw new ConstraintViolationException("Invalid categories list", null);
        }
        LocalDateTime start =
                (se.getRangeStart() == null) ? LocalDateTime.now() :
                        LocalDateTime.parse(se.getRangeStart(), DateConfig.FORMATTER);
        LocalDateTime end =
                (se.getRangeEnd() != null) ? LocalDateTime.parse(se.getRangeEnd(), DateConfig.FORMATTER) : null;
        if (end != null && !end.isAfter(start))
            throw new ConstraintViolationException(dateErrorMessage, null);

    }
}
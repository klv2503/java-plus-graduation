package ru.practicum.events.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.logging.log4j.util.Strings;
import ru.practicum.config.DateConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class TimeNotEarlyValidator implements ConstraintValidator<TimeNotEarly, String> {
    private int hours;

    @Override
    public void initialize(TimeNotEarly constraintAnnotation) {
        this.hours = constraintAnnotation.hours();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (Strings.isEmpty(value)) {
            return false;
        }
        try {
            LocalDateTime appointedDate = LocalDateTime.parse(value, DateConfig.FORMATTER);
            LocalDateTime minDate = LocalDateTime.now().plusHours(hours);
            return !appointedDate.isBefore(minDate);
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
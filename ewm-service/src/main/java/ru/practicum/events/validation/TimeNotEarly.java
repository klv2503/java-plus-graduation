package ru.practicum.events.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TimeNotEarlyValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TimeNotEarly {
    String message();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int hours(); // Число часов, на которое должно быть позже время
}
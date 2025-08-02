package ru.practicum.users.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

//Аннотация для проверки полей запроса на update User - каждое из полей может быть пустым, но не оба одновременно
//За проверку отвечает ValidateUserDtoForUpdate
@Documented
@Constraint(validatedBy = ValidateUserDtoForUpdate.class)
@Target(value = {ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmptyUserDto {

    String message() default "At least one of (name and email) must not be empty";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

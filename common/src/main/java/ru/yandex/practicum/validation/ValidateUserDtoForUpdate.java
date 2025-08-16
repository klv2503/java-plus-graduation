package ru.yandex.practicum.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.logging.log4j.util.Strings;
import ru.yandex.practicum.dto.user.UserDto;

public class ValidateUserDtoForUpdate implements ConstraintValidator<NotEmptyUserDto, UserDto> {
    @Override
    public boolean isValid(UserDto userDto, ConstraintValidatorContext constraintValidatorContext) {
        return !Strings.isBlank(userDto.getName()) ||
                !Strings.isBlank(userDto.getEmail());
    }
}

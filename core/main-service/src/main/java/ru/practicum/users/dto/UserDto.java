package ru.practicum.users.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.users.validation.NotEmptyUserDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@NotEmptyUserDto
public class UserDto {

    @NotNull
    @Positive
    private Long id; // уникальный идентификатор пользователя;

    @Size(max = 200)
    private String name; // имя или логин пользователя;

    @Size(max = 50)
    private String email; // адрес электронной почты

}

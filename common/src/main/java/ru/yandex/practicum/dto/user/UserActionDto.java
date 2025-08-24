package ru.yandex.practicum.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.enums.ActionType;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActionDto {

    private long userId;

    private long eventId;

    private ActionType actionType;

    private Instant timestamp;
}

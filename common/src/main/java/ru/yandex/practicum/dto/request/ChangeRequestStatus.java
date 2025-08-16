package ru.yandex.practicum.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.enums.ParticipationRequestStatus;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeRequestStatus {

    private Long event;

    private int limit;

    private List<Long> requestIds;

    private ParticipationRequestStatus status;
}

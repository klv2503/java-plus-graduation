package ru.yandex.practicum.dto.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.yandex.practicum.config.DateConfig;
import ru.yandex.practicum.dto.location.LocationDto;
import ru.yandex.practicum.enums.StateEvent;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EventFullDto extends EventShortDto {
    private String createdOn;
    private String description;
    private LocationDto location;
    private int participantLimit;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateConfig.FORMAT)
    private String publishedOn;
    private boolean requestModeration;
    private StateEvent state;
}

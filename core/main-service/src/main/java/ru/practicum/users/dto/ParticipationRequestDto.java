package ru.practicum.users.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.config.DateConfig;
import ru.practicum.users.model.ParticipationRequestStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {

    @NotNull
    @Positive
    private Long id;

    @NotNull
    @Positive
    private Long requester;

    @NotNull
    @Positive
    private Long event;

    @NotNull
    private ParticipationRequestStatus status;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateConfig.FORMAT)
    private LocalDateTime created;
}
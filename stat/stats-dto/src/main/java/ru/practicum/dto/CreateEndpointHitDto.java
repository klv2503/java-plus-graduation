package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.config.DateConfig;
import ru.practicum.validation.CreateEndpointHitDtoValidation;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateEndpointHitDto implements CreateEndpointHitDtoValidation {
    private String app;
    private String uri;
    private String ip;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateConfig.FORMAT)
    private LocalDateTime timestamp;
}

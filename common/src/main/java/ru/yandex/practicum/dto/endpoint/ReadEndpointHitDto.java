package ru.yandex.practicum.dto.endpoint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadEndpointHitDto {
    private String app;
    private String uri;
    private int hits;
}

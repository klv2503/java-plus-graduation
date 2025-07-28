package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TakeHitsDto {

    private LocalDateTime start;

    private LocalDateTime end;

    private List<String> uris;

    private boolean unique;
}

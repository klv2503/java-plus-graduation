package ru.practicum.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchEventsParams {

    private String text;

    private List<Long> categories;

    private Boolean paid;

    private String rangeStart;

    private String rangeEnd;

    private Boolean onlyAvailable;

    private String sort;

    private int from;

    private int size;

}
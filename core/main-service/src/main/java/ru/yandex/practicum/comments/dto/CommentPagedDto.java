package ru.yandex.practicum.comments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class CommentPagedDto {
    private List<CommentDto> comments;
    private int page;
    private int total;
}
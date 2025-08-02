package ru.practicum.comments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class CommentPagedDto {
    private List<CommentOutputDto> comments;
    private int page;
    private int total;
}
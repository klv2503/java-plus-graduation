package ru.practicum.comments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.comments.model.CommentsStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentEconomDto {

    private Long id;

    private Long userId;

    private Long eventId;

    private String text;

    private LocalDateTime created;

    private CommentsStatus status;

}

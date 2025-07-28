package ru.practicum.comments.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.comments.model.CommentsStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDto {
    private Long id;

    private Long userId;

    private Long eventId;

    @NotBlank
    private String text;

    private LocalDateTime created;

    private CommentsStatus status;

}

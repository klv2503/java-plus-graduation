package ru.yandex.practicum.comments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.comments.model.CommentsStatus;
import ru.yandex.practicum.events.dto.EventShortDto;
import ru.yandex.practicum.users.model.User;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentOutputDto {

    private Long id;

    private User user;

    private EventShortDto event;

    private String text;

    private LocalDateTime created;

    private CommentsStatus status;

}

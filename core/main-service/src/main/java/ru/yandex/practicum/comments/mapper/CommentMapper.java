package ru.yandex.practicum.comments.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.comments.dto.CommentEconomDto;
import ru.yandex.practicum.comments.dto.CommentOutputDto;
import ru.yandex.practicum.comments.model.Comment;
import ru.yandex.practicum.events.mapper.EventMapper;
import ru.yandex.practicum.users.model.User;

@Component
public class CommentMapper {

    public static CommentOutputDto commentToOutputDto(Comment comment) {
        User user = User.builder()
                .id(comment.getUser().getId())
                .email(comment.getUser().getEmail())
                .name(comment.getUser().getName())
                .build();

        return CommentOutputDto.builder()
                .id(comment.getId())
                .user(user)
                .event(EventMapper.toEventShortDto(comment.getEvent()))
                .text(comment.getText())
                .created(comment.getCreated())
                .status(comment.getStatus())
                .build();
    }

    public static CommentEconomDto commentToEconomDto(Comment comment) {
        return CommentEconomDto.builder()
                .id(comment.getId())
                .userId(comment.getUser().getId())
                .eventId(comment.getEvent().getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .status(comment.getStatus())
                .build();
    }
}

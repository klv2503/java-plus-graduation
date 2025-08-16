package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.CommentDto;
import ru.yandex.practicum.model.Comment;

@Component
public class CommentMapper {

    public static CommentDto commentToDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .eventId(comment.getEvent())
                .text(comment.getText())
                .created(comment.getCreated())
                .status(comment.getStatus())
                .build();
    }

}

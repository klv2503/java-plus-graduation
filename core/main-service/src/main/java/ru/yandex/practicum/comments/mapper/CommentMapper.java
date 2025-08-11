package ru.yandex.practicum.comments.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.comments.dto.CommentEconomDto;
import ru.yandex.practicum.comments.dto.CommentOutputDto;
import ru.yandex.practicum.comments.model.Comment;
import ru.yandex.practicum.events.mapper.EventMapper;

@Component
public class CommentMapper {

    public static CommentOutputDto commentToOutputDto(Comment comment) {
        return CommentOutputDto.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .event(EventMapper.toEventShortDto(comment.getEvent()))
                .text(comment.getText())
                .created(comment.getCreated())
                .status(comment.getStatus())
                .build();
    }

    public static CommentEconomDto commentToEconomDto(Comment comment) {
        return CommentEconomDto.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .eventId(comment.getEvent().getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .status(comment.getStatus())
                .build();
    }
}

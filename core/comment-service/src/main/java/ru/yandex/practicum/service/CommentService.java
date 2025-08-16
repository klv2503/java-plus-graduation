package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.CommentDto;
import ru.yandex.practicum.dto.CommentPagedDto;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.model.CommentsOrder;

public interface CommentService {

    CommentPagedDto getComments(Long eventId, int page, int size, CommentsOrder sort);

    CommentDto addComment(Long userId, CommentDto commentDto);

    CommentDto updateComment(CommentDto dto);

    Comment getComment(Long id);

    void softDelete(Long userId, Long commentId);

    void deleteById(Long commentId);
}

package ru.yandex.practicum.comments.service;

import ru.yandex.practicum.comments.dto.CommentDto;
import ru.yandex.practicum.comments.dto.CommentPagedDto;
import ru.yandex.practicum.comments.model.Comment;
import ru.yandex.practicum.comments.model.CommentsOrder;

public interface CommentService {

    CommentPagedDto getComments(Long eventId, int page, int size, CommentsOrder sort);

    CommentDto addComment(Long userId, CommentDto commentDto);

    CommentDto updateComment(CommentDto dto);

    Comment getComment(Long id);

    void softDelete(Long userId, Long commentId);

    void deleteById(Long commentId);
}

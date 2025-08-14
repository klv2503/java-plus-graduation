package ru.yandex.practicum.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.clients.EventServiceFeign;
import ru.yandex.practicum.clients.UserServiceFeign;
import ru.yandex.practicum.dto.CommentDto;
import ru.yandex.practicum.dto.CommentPagedDto;
import ru.yandex.practicum.mapper.CommentMapper;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.model.CommentsOrder;
import ru.yandex.practicum.model.CommentsStatus;
import ru.yandex.practicum.repository.CommentRepository;
import ru.yandex.practicum.errors.exceptions.AccessDeniedException;
import ru.yandex.practicum.errors.exceptions.ForbiddenActionException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final UserServiceFeign userServiceFeign;

    private final EventServiceFeign eventServiceFeign;

    private final CommentRepository commentRepository;

    @Override
    public CommentPagedDto getComments(Long eventId, int page, int size, CommentsOrder sort) {
        if (eventId == null || eventId <= 0)
            throw new IllegalArgumentException("Event ID must be a positive number.");
        if (page <= 0)
            throw new IllegalArgumentException("Page number must be positive and greater than 0.");
        if (size <= 0)
            throw new IllegalArgumentException("Page size must be positive and greater than 0.");
        if (sort == null)
            throw new IllegalArgumentException("Sort parameter cannot be null.");

        eventServiceFeign.getEventInfo(eventId);

        Sort sortType = sort == CommentsOrder.NEWEST ?
                Sort.by("id").descending() : Sort.by("id").ascending();

        Pageable pageable = PageRequest.of(page - 1, size, sortType);

        Page<Comment> commentPage = commentRepository
                .findByEventAndStatus(eventId, CommentsStatus.PUBLISHED, pageable);

        List<CommentDto> comments = commentPage.getContent().stream()
                .map(CommentMapper::commentToDto)
                .collect(Collectors.toList());

        return CommentPagedDto.builder()
                .page(page)
                .total(commentPage.getTotalPages())
                .comments(comments)
                .build();
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, CommentDto commentDto) {
        userServiceFeign.getUserById(userId); //проверка существования юзера
        eventServiceFeign.getEventInfo(commentDto.getEventId());
        Comment comment = Comment.builder()
                .userId(userId)
                .event(commentDto.getEventId())
                .text(commentDto.getText())
                .created(LocalDateTime.now())
                .status(CommentsStatus.PUBLISHED)
                .build();
        return CommentMapper.commentToDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto updateComment(CommentDto dto) {
        Comment comment = getComment(dto.getId());
        if (!comment.getUserId().equals(dto.getUserId())) {
            throw new AccessDeniedException("User " + dto.getUserId() + "can't edit this comment.");
        }
        comment.setText(dto.getText());
        log.info("CommentServiceImpl: Comment for update {}", comment);
        return CommentMapper.commentToDto(commentRepository.save(comment));
    }

    @Override
    public Comment getComment(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with " + id + " not found"));
    }

    @Transactional
    @Override
    public void deleteById(Long commentId) {
        Comment comment = getComment(commentId);

        if (!comment.getStatus().equals(CommentsStatus.PUBLISHED)) {
            commentRepository.deleteById(commentId);
        } else {
            throw new ForbiddenActionException("The comment's status doesn't allow it to be deleted");
        }
    }

    @Transactional
    @Override
    public void softDelete(Long userId, Long commentId) {
        Comment comment = getComment(commentId);

        if (!comment.getUserId().equals(userId)) {
           throw new AccessDeniedException("Not enough rights");
        }

        comment.setStatus(CommentsStatus.DELETED);
        commentRepository.save(comment);
    }
}

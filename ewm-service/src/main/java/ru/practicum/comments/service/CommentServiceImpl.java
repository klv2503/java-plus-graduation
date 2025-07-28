package ru.practicum.comments.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.CommentEconomDto;
import ru.practicum.comments.dto.CommentOutputDto;
import ru.practicum.comments.dto.CommentPagedDto;
import ru.practicum.comments.mapper.CommentMapper;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.model.CommentsOrder;
import ru.practicum.comments.model.CommentsStatus;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.errors.AccessDeniedException;
import ru.practicum.errors.ForbiddenActionException;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.events.service.PublicEventsService;
import ru.practicum.users.service.AdminUserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final AdminUserService adminUserService;

    private final PublicEventsService publicEventsService;

    private final EventRepository eventRepository;

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

        eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with " + id + " not found"));

        Sort sortType = sort == CommentsOrder.NEWEST ?
                Sort.by("id").descending() : Sort.by("id").ascending();

        Pageable pageable = PageRequest.of(page - 1, size, sortType);

        Page<Comment> commentPage = commentRepository
                .findByEventIdAndStatus(eventId, CommentsStatus.PUBLISHED, pageable);

        List<CommentOutputDto> comments = commentPage.getContent().stream()
                .map(CommentMapper::commentToOutputDto)
                .collect(Collectors.toList());

        return CommentPagedDto.builder()
                .page(page)
                .total(commentPage.getTotalPages())
                .comments(comments)
                .build();
    }

    @Override
    @Transactional
    public CommentEconomDto addComment(Long userId, CommentDto commentDto) {
        Comment comment = Comment.builder()
                .user(adminUserService.getUser(userId))
                .event(publicEventsService.getEventAnyStatusWithViews(commentDto.getEventId()))
                .text(commentDto.getText())
                .created(LocalDateTime.now())
                .status(CommentsStatus.PUBLISHED)
                .build();
        return CommentMapper.commentToEconomDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentEconomDto updateComment(CommentDto dto) {
        Comment comment = getComment(dto.getId());
        if (!comment.getUser().getId().equals(dto.getUserId())) {
            throw new AccessDeniedException("User " + dto.getUserId() + "can't edit this comment.");
        }
        comment.setText(dto.getText());
        log.info("CommentServiceImpl: Comment for update {}", comment);
        return CommentMapper.commentToEconomDto(commentRepository.save(comment));
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

        if (!comment.getUser().getId().equals(userId)) {
           throw new AccessDeniedException("Not enough rights");
        }

        comment.setStatus(CommentsStatus.DELETED);
        commentRepository.save(comment);
    }
}

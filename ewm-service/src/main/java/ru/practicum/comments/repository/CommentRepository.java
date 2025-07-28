package ru.practicum.comments.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.model.CommentsStatus;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByEventIdAndStatus(Long eventId, CommentsStatus status, Pageable pageable);
}

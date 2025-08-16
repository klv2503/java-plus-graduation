package ru.yandex.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.model.CommentsStatus;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByEventAndStatus(Long eventId, CommentsStatus status, Pageable pageable);
}

package ru.practicum.comments;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.model.CommentsStatus;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.comments.service.CommentService;
import ru.practicum.errors.AccessDeniedException;
import ru.practicum.errors.ForbiddenActionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "classpath:application-test.properties")
@Transactional(readOnly = true)
public class CommentsIntegrationTest {

    @Autowired
    CommentService commentService;
    @Autowired
    CommentRepository commentRepository;

    @Test
    @Transactional
    public void softDeleteComment() {
        commentService.softDelete(1L, 1L);
        Comment comment = commentRepository.findById(1L).get();

        assertEquals(CommentsStatus.DELETED, comment.getStatus());
    }

    @Test
    public void softDeleteWrongCommentId() {
        assertThrows(EntityNotFoundException.class, () -> commentService.softDelete(1L, 1000L));
    }

    @Test
    public void softDeleteWrongUserId() {
        assertThrows(AccessDeniedException.class, () -> commentService.softDelete(10L, 1L));
    }

    @Test
    @Transactional
    public void deleteCommentWithStatusDeletedTest() {
        commentService.deleteById(2L);

        assertFalse(commentRepository.existsById(2L));
    }

    @Test
    public void deleteCommentWithStatusPublished() {
        assertThrows(ForbiddenActionException.class, () -> commentService.deleteById(3L));
    }

    @Test
    @Transactional
    public void deleteCommentWithStatusBanned() {
        commentService.deleteById(4L);

        assertFalse(commentRepository.existsById(4L));
    }
}

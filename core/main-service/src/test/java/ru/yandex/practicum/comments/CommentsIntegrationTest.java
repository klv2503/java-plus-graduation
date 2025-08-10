package ru.yandex.practicum.comments;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.comments.model.Comment;
import ru.yandex.practicum.comments.model.CommentsStatus;
import ru.yandex.practicum.comments.repository.CommentRepository;
import ru.yandex.practicum.comments.service.CommentService;
import ru.yandex.practicum.errors.AccessDeniedException;
import ru.yandex.practicum.errors.ForbiddenActionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
public class CommentsIntegrationTest {

    @Autowired
    CommentService commentService;
    @Autowired
    CommentRepository commentRepository;

    @Test
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
    public void deleteCommentWithStatusDeletedTest() {
        commentService.deleteById(2L);

        assertFalse(commentRepository.existsById(2L));
    }

    @Test
    public void deleteCommentWithStatusPublished() {

        assertThrows(ForbiddenActionException.class, () -> commentService.deleteById(3L));
    }

    @Test
    public void deleteCommentWithStatusBanned() {
        commentService.deleteById(4L);

        assertFalse(commentRepository.existsById(4L));
    }
}

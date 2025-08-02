package ru.practicum.comments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.service.CommentService;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Slf4j
public class AdminCommentController {

    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    ResponseEntity<Void> delete(@PathVariable Long commentId) {
        log.info("Request for delete comment with id {}", commentId);
        commentService.deleteById(commentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

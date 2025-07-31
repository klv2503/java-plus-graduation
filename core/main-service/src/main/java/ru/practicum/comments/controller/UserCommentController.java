package ru.practicum.comments.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.CommentEconomDto;
import ru.practicum.comments.service.CommentService;

@RestController
@RequestMapping("/users/{userId}/comments")
@Validated
@RequiredArgsConstructor
@Slf4j
public class UserCommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentEconomDto> addComment(@PathVariable
                                                       @Min(value = 1, message = "ID must be positive") Long userId,
                                                       @RequestBody @Valid CommentDto dto) {
        log.info("\nCommentController.addComment accepted userId {}, dto {}", userId, dto);
        CommentEconomDto createdComment = commentService.addComment(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentEconomDto> updateComment(@PathVariable
                                                          @Min(value = 1, message = "ID must be positive") Long userId,
                                                          @PathVariable
                                                          @Min(value = 1, message = "ID must be positive") Long commentId,
                                                          @RequestBody @Valid CommentDto dto) {
        log.info("UserCommentController: accepted userId {}, commentId {}, text{}", userId, commentId, dto);
        dto.setId(commentId);
        dto.setUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(commentService.updateComment(dto));
    }

    @DeleteMapping("/{commentId}")
    ResponseEntity<Void> softDelete(@PathVariable Long userId, @PathVariable Long commentId) {
        log.info("Request for soft delete comment with id {} by user with id {}", commentId, userId);
        commentService.softDelete(userId, commentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

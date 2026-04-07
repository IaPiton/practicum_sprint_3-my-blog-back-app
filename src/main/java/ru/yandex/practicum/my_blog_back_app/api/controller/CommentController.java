package ru.yandex.practicum.my_blog_back_app.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.CommentCreateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.CommentUpdateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.CommentResponse;
import ru.yandex.practicum.my_blog_back_app.core.service.CommentService;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(@PathVariable("postId") String postId) {
        if ("undefined".equals(postId)) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        if (commentService.postExists(Long.parseLong(postId))) {
            return ResponseEntity.ok(commentService.getCommentsByPostId(Long.parseLong(postId)));
        }

        throw new IllegalArgumentException(String.format("Пост с id: %s не найден", postId));
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getCommentById(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId) {

        if (!commentService.postExists(postId)) {
            throw new IllegalArgumentException(String.format("Пост с id: %d не найден", postId));
        }

        CommentResponse comment = commentService.getCommentById(commentId);

        return ResponseEntity.ok(comment);
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable("postId") Long postId,
            @Valid @RequestBody CommentCreateRequest request) {

        if (commentService.postExists(postId)) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(commentService.createComment(request));
        }
        throw new IllegalArgumentException(String.format("Пост с id: %d не найден", postId));

    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
             @PathVariable("commentId") Long commentId,
            @Valid @RequestBody CommentUpdateRequest request) {

        if (!commentService.postExists(request.getPostId())) {
            throw new IllegalArgumentException(String.format("Пост с id: %d не найден", request.getPostId()));
        }

        return ResponseEntity.ok(commentService.updateComment(commentId, request));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId) {

        if (!commentService.postExists(postId)) {
            throw new IllegalArgumentException(String.format("Пост с id: %d не найден", postId));
        }

        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }
}
package ru.yandex.practicum.my_blog_back_app.api.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.CommentCreateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.CommentUpdateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.CommentResponse;
import ru.yandex.practicum.my_blog_back_app.core.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(@PathVariable Long postId) {
        if (!commentService.postExists(postId)) {
            throw new IllegalArgumentException("Пост с id: " + postId + " не найден");
        }

        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getCommentById(
            @PathVariable Long postId,
            @PathVariable Long commentId) {

        if (!commentService.postExists(postId)) {
            throw new IllegalArgumentException(String.format("Пост с id: %d не найден", postId));
        }

        CommentResponse comment = commentService.getCommentById(commentId);

        if (!comment.getPostId().equals(postId)) {
            throw new IllegalArgumentException(
                    String.format("Комментарий с id: %d не принадлежит посту c id: %d", commentId, postId));
        }

        return ResponseEntity.ok(comment);
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request) {

        if (!postId.equals(request.getPostId())) {
            throw new IllegalArgumentException(
                    String.format("Пост с id: %d из запроса не совпадает с id: %d из тела", postId, request.getPostId()));
        }

        if (!commentService.postExists(postId)) {
            throw new IllegalArgumentException(String.format("Пост с id: %d не найден", postId));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.createComment(request));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request) {

        if (!postId.equals(request.getPostId())) {
            throw new IllegalArgumentException(
                    String.format("Пост с id: %d из запроса не совпадает с id: %d из тела", postId, request.getPostId()));
        }

        if (!commentService.postExists(postId)) {
            throw new IllegalArgumentException(String.format("Пост с id: %d не найден", postId));
        }

        return ResponseEntity.ok(commentService.updateComment(commentId, request));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {

        if (!commentService.postExists(postId)) {
            throw new IllegalArgumentException(String.format("Пост с id: %d не найден", postId));
        }

        CommentResponse comment = commentService.getCommentById(commentId);

        if (!comment.getPostId().equals(postId)) {
            throw new IllegalArgumentException(
                    String.format("Комментарий с id: %d не принадлежит посту c id: %d", commentId, postId));
        }

        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getCommentsCount(@PathVariable Long postId) {

        if (!commentService.postExists(postId)) {
            throw new IllegalArgumentException(String.format("Пост с id: %d не найден", postId));
        }

        return ResponseEntity.ok(commentService.getCommentsCountByPostId(postId));
    }


}

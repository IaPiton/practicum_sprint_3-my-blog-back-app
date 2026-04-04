package ru.yandex.practicum.my_blog_back_app.persistence.repository;

public interface CommentRepository {
    Long countCommentsByPost(Long postId);
}

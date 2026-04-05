package ru.yandex.practicum.my_blog_back_app.persistence.repository;

import ru.yandex.practicum.my_blog_back_app.persistence.entity.CommentsEntity;

import java.util.List;

public interface CommentRepository {
    Long countCommentsByPost(Long postId);
    boolean postExists(Long postId);
    List<CommentsEntity> findCommentsByPostId(Long postId);
    Long save(CommentsEntity commentsEntity);
    CommentsEntity findById(Long commentId);
    void update(CommentsEntity commentsEntity);
    void deleteByCommentId(Long commentId);
    void deleteByPostId(Long postId);
}
package ru.yandex.practicum.my_blog_back_app.core.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.CommentCreateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.CommentUpdateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.CommentResponse;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService{
    @Override
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        return List.of();
    }

    @Override
    public CommentResponse getCommentById(Long commentId) {
        return null;
    }

    @Override
    public CommentResponse createComment(CommentCreateRequest request) {
        return null;
    }

    @Override
    public CommentResponse updateComment(Long commentId, CommentUpdateRequest request) {
        return null;
    }

    @Override
    public void deleteComment(Long commentId) {

    }

    @Override
    public boolean postExists(Long postId) {
        return false;
    }

    @Override
    public Long getCommentsCountByPostId(Long postId) {
        return 0L;
    }
}

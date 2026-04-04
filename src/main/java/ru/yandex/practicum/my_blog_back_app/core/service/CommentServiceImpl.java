package ru.yandex.practicum.my_blog_back_app.core.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.CommentCreateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.CommentUpdateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.CommentResponse;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.CommentsEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.repository.CommentRepository;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;

    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        List<CommentsEntity> commentsEntities = commentRepository.findCommentsByPostId(postId);
        return commentsEntities.stream()
                .map(entity -> CommentResponse.builder()
                        .id(entity.getId())
                        .text(entity.getText())
                        .postId(entity.getPostId())
                        .build())
                .toList();
    }

    @Override
    public CommentResponse getCommentById(Long commentId) {
        CommentsEntity commentsEntity = commentRepository.findById(commentId);
        return CommentResponse.builder()
                .id(commentsEntity.getId())
                .text(commentsEntity.getText())
                .postId(commentsEntity.getPostId())
                .build();
    }

    @Override
    public CommentResponse createComment(CommentCreateRequest request) {
        CommentsEntity commentsEntity = new CommentsEntity();
        commentsEntity.setPostId(request.getPostId());
        commentsEntity.setText(request.getText());
        commentsEntity.setId(commentRepository.save(commentsEntity));
        return CommentResponse.builder()
                .id(commentsEntity.getId())
                .text(commentsEntity.getText())
                .postId(commentsEntity.getPostId())
                .build();
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
        return commentRepository.postExists(postId);
    }

    @Override
    public Long getCommentsCountByPostId(Long postId) {
        return 0L;
    }
}

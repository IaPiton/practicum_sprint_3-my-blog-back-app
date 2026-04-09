package ru.yandex.practicum.my_blog_back_app.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.CommentCreateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.CommentUpdateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.CommentResponse;
import ru.yandex.practicum.my_blog_back_app.api.handler.EntityNotFoundException;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.CommentEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.mapper.CommentMapper;
import ru.yandex.practicum.my_blog_back_app.persistence.repository.CommentRepository;
import ru.yandex.practicum.my_blog_back_app.persistence.repository.PostRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final PostRepository postRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        List<CommentEntity> commentsEntities = commentRepository.findCommentsByPostId(postId);
        return commentsEntities.stream()
                .map(commentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getCommentById(Long commentId) {
        CommentEntity commentsEntity = commentRepository.findById(commentId)
                .orElseThrow(EntityNotFoundException::new);
        return commentMapper.toResponse(commentsEntity);
    }

    @Override
    public CommentResponse createComment(CommentCreateRequest request) {
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setPost(postRepository.findById(request.getPostId())
                .orElseThrow(EntityNotFoundException::new));
        commentEntity.setText(request.getText());
        commentEntity = commentRepository.save(commentEntity);
        return commentMapper.toResponse(commentEntity);
    }

    @Override
    public CommentResponse updateComment(Long commentId, CommentUpdateRequest request) {
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(EntityNotFoundException::new);
        commentEntity.setText(request.getText());
        commentEntity = commentRepository.save(commentEntity);
        return commentMapper.toResponse(commentEntity);
    }

    @Override
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean postExists(Long postId) {
        return commentRepository.existsByPostId(postId);
    }
}
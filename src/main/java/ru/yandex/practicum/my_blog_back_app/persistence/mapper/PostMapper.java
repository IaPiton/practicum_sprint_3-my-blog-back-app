package ru.yandex.practicum.my_blog_back_app.persistence.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.PostCreateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostPreview;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostResponse;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.PostEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.TagEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.repository.CommentRepository;


@Component
@RequiredArgsConstructor
public class PostMapper {
    private final CommentRepository commentRepository;

    public PostResponse toResponse(PostEntity postEntity) {
        return PostResponse.builder()
                .id(postEntity.getId())
                .title(postEntity.getTitle())
                .text(postEntity.getText())
                .likesCount(postEntity.getLikesCount())
                .tags(postEntity.getTags().stream().map(TagEntity::getName).toList())
//                .commentsCount(commentRepository.countCommentsByPost(postEntity.getId()))
                .image(postEntity.getImage())
                .build();
    }

    public PostPreview toPreviewResponse(PostEntity postEntity, String truncatedText) {
       return PostPreview.builder()
                .id(postEntity.getId())
                .title(postEntity.getTitle())
                .text(truncatedText)
                .tags(postEntity.getTags().stream().map(TagEntity::getName).toList())
                .likesCount(postEntity.getLikesCount())
//                .commentsCount(commentRepository.countCommentsByPost(postEntity.getId()))
                .build();
    }

    public PostEntity toEntity(PostCreateRequest request) {
        PostEntity postEntity = new PostEntity();
        postEntity.setTitle(request.getTitle());
        postEntity.setText(request.getText());
        postEntity.setLikesCount(0L);
        return postEntity;
    }
}
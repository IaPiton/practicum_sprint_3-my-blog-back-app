package ru.yandex.practicum.my_blog_back_app.persistence.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.CommentResponse;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.CommentsEntity;

@Component
public class CommentMapper {

    public CommentResponse toResponse(CommentsEntity commentsEntity) {
        return CommentResponse.builder()
                .id(commentsEntity.getId())
                .text(commentsEntity.getText())
                .postId(commentsEntity.getPostId())
                .build();
    }
}
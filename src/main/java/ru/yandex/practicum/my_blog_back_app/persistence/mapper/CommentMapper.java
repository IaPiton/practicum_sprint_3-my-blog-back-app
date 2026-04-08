package ru.yandex.practicum.my_blog_back_app.persistence.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.CommentResponse;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.CommentEntity;

@Component
public class CommentMapper {

    public CommentResponse toResponse(CommentEntity commentEntity) {
        return CommentResponse.builder()
                .id(commentEntity.getId())
                .text(commentEntity.getText())
                .postId(commentEntity.getPost().getId())
                .build();
    }
}
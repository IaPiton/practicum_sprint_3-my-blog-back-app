package ru.yandex.practicum.my_blog_back_app.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PostPreview {
    private Long id;
    private String title;
    private String text;
    private List<String> tags;
    private Long likesCount;
    private Long commentsCount;
}
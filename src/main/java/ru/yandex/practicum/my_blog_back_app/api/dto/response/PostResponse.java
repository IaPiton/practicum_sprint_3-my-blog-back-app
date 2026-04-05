package ru.yandex.practicum.my_blog_back_app.api.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private byte[] image;
    private String title;
    private String text;
    private List<String> tags;
    private Long likesCount;
    private Long commentsCount;
}
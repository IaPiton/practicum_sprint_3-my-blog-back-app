package ru.yandex.practicum.my_blog_back_app.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private byte[] image;
    private String title;
    private String text;
    private List<String> tags = new ArrayList<>();
    private Long likesCount;
    private Long commentsCount;
}
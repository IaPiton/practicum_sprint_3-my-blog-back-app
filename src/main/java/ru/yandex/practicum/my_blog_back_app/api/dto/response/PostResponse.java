package ru.yandex.practicum.my_blog_back_app.api.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String text;
    private List<String> tags = new ArrayList<>();
    private Long likesCount;
    private Long commentsCount;

    public PostResponse(Long id,
                        String title,
                        String text,
                        List<String> tags,
                        Long likesCount,
                        Long commentsCount) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
    }
}
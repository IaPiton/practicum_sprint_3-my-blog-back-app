package ru.yandex.practicum.my_blog_back_app.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String text;
    private Long postId;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
}
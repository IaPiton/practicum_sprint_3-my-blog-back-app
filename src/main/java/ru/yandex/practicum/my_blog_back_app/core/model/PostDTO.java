package ru.yandex.practicum.my_blog_back_app.core.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostDTO {
    private Long id;
    private String title;
    private String text;
    private Long likesCount;
    private byte[] image;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
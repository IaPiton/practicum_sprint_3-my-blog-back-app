package ru.yandex.practicum.my_blog_back_app.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateRequest {
    private Long id;

    @NotBlank(message = "Text пустой")
    private String text;


    private Long postId;
}
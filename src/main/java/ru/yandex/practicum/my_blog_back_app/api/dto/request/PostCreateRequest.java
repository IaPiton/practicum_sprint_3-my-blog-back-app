package ru.yandex.practicum.my_blog_back_app.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {
    @NotBlank(message = "Title пустой")
    private String title;

    @NotBlank(message = "Text пустой")
    private String text;

    @NotNull(message = "Tags не может быть пустым")
    @NotEmpty(message = "Tags не может быть пустым")
    private List<String> tags;
}
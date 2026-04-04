package ru.yandex.practicum.my_blog_back_app.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
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
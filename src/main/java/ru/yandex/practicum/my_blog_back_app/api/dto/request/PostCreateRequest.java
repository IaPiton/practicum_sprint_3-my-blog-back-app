package ru.yandex.practicum.my_blog_back_app.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {
    @NotBlank(message = "Title пустой")
    private String title;

    @NotBlank(message = "Text пустой")
    private String text;

    private List<String> tags = new ArrayList<>();
}
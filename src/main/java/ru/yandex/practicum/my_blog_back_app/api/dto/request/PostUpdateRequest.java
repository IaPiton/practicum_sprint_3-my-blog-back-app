package ru.yandex.practicum.my_blog_back_app.api.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PostUpdateRequest {
    @NotNull(message = "ID пустой")
    private Long id;

    @NotNull(message = "Title пустой")
    private String title;

    @NotNull(message = "Text пустой")
    private String text;

    private List<String> tags = new ArrayList<>();

}

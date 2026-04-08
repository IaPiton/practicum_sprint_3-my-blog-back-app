package ru.yandex.practicum.my_blog_back_app.api.dto.request;

//import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequest {
//    @NotNull(message = "ID пустой")
    private Long id;

//    @NotNull(message = "Title пустой")
    private String title;

//    @NotNull(message = "Text пустой")
    private String text;

    private List<String> tags = new ArrayList<>();
}
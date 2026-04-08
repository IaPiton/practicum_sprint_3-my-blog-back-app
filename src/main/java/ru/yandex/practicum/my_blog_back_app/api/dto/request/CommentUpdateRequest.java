package ru.yandex.practicum.my_blog_back_app.api.dto.request;

//import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateRequest {
    private Long id;

//    @NotBlank(message = "Text пустой")
    private String text;

    private Long postId;
}
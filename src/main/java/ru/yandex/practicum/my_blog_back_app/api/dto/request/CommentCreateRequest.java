package ru.yandex.practicum.my_blog_back_app.api.dto.request;

//import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {
//    @NotBlank(message = "Text пустой")
    private String text;
    private Long postId;
}
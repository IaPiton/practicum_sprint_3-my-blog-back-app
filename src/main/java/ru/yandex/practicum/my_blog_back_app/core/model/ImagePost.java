package ru.yandex.practicum.my_blog_back_app.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImagePost {
    private byte[] image;
    private MediaType contentType;
}

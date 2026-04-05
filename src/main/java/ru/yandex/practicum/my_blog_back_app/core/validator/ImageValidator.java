package ru.yandex.practicum.my_blog_back_app.core.validator;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.my_blog_back_app.api.handler.InvalidImageException;

@Component
public class ImageValidator {
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    public void validate(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new InvalidImageException("Image file is empty");
        }
        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new InvalidImageException("Image size exceeds 5MB");
        }
    }
}
package ru.yandex.practicum.my_blog_back_app.api.handler;

public class InvalidImageException extends RuntimeException {
    public InvalidImageException(String message) {
        super(message);
    }
}

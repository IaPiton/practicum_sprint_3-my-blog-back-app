package ru.yandex.practicum.my_blog_back_app.persistence.repository;

import ru.yandex.practicum.my_blog_back_app.persistence.entity.PostEntity;

public interface PostRepository {
    PostEntity savePost(PostEntity postEntity);
}

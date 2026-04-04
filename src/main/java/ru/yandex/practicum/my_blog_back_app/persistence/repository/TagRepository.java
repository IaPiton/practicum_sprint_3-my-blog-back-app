package ru.yandex.practicum.my_blog_back_app.persistence.repository;

import jakarta.validation.constraints.NotNull;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.PostEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.TagEntity;

import java.util.List;

public interface TagRepository {
    List<TagEntity> getTags(List<String> tags);

    void saveTagsAndPost(PostEntity postEntity);

    List<TagEntity> findTagsByPostId(Long postId);

    void deleteTagAndPost(Long postId);
}

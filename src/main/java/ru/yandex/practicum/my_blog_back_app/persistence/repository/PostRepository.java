package ru.yandex.practicum.my_blog_back_app.persistence.repository;

import ru.yandex.practicum.my_blog_back_app.persistence.entity.PostEntity;

import java.util.List;

public interface PostRepository {
    PostEntity savePost(PostEntity postEntity);

    List<PostEntity> findPostsWithFilters(String titleSubstring, List<String> tags, int pageSize, int offset);

    int countPostsWithFilters(String titleSubstring, List<String> tags);

    PostEntity findById(Long postId);

    void update(PostEntity post);

    void delete(Long postId);
}

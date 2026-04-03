package ru.yandex.practicum.my_blog_back_app.core.service;

import ru.yandex.practicum.my_blog_back_app.api.dto.request.PostCreateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.PostUpdateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostListResponse;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostResponse;
import ru.yandex.practicum.my_blog_back_app.core.model.ImagePost;

public interface PostService {
    PostListResponse getPosts(String search, int pageNumber, int pageSize);
    PostResponse getPostById(Long id);
    PostResponse createPost(PostCreateRequest request);
    PostResponse updatePost(Long id, PostUpdateRequest request);
    void deletePost(Long id);
    Long incrementLikes(Long id);
    void updatePostImage(Long id, byte[] image);
    ImagePost getPostImage(Long id);
}
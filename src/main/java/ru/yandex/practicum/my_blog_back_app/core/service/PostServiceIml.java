package ru.yandex.practicum.my_blog_back_app.core.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.PostCreateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.PostUpdateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostListResponse;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostResponse;
import ru.yandex.practicum.my_blog_back_app.core.model.ImagePost;

@Service
public class PostServiceIml implements PostService{
    @Override
    public PostListResponse getPosts(String search, int pageNumber, int pageSize) {
        return new PostListResponse();
    }

    @Override
    public PostResponse getPostById(Long id) {
        return null;
    }

    @Override
    public PostResponse createPost(PostCreateRequest request) {
        return null;
    }

    @Override
    public PostResponse updatePost(Long id, PostUpdateRequest request) {
        return null;
    }

    @Override
    public void deletePost(Long id) {

    }

    @Override
    public Long incrementLikes(Long id) {
        return 0L;
    }

    @Override
    public void updatePostImage(Long id, byte[] image) {

    }

    @Override
    public ImagePost getPostImage(Long id) {
        return null;
    }
}

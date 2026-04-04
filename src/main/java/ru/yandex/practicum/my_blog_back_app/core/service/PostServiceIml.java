package ru.yandex.practicum.my_blog_back_app.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.PostCreateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.PostUpdateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostListResponse;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostResponse;
import ru.yandex.practicum.my_blog_back_app.core.model.ImagePost;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.PostEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.TagEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.repository.PostRepository;
import ru.yandex.practicum.my_blog_back_app.persistence.repository.TagRepository;

import java.util.List;

@Service
public class PostServiceIml implements PostService {
    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    @Autowired
    public PostServiceIml(PostRepository postRepository,
                          TagRepository tagRepository) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
    }

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
        PostEntity postEntity = new PostEntity();
        postEntity.setTitle(request.getTitle());
        postEntity.setText(request.getText());
        postEntity.setLikesCount(0L);

        List<TagEntity> tags = tagRepository.getTags(request.getTags());

        postEntity.setTags(tags);
        postEntity = postRepository.savePost(postEntity);

        return PostResponse.builder()
                .id(postEntity.getId())
                .title(postEntity.getTitle())
                .text(postEntity.getText())
                .likesCount(postEntity.getLikesCount())
                .tags(postEntity.getTags().stream().map(TagEntity::getName).toList())
                .commentsCount(0L)
                .build();
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

package ru.yandex.practicum.my_blog_back_app.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.PostCreateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.PostUpdateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostListResponse;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostResponse;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostPreview;
import ru.yandex.practicum.my_blog_back_app.core.model.SearchCriteria;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.PostEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.TagEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.repository.CommentRepository;
import ru.yandex.practicum.my_blog_back_app.persistence.repository.PostRepository;
import ru.yandex.practicum.my_blog_back_app.persistence.repository.TagRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PostServiceIml implements PostService {
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public PostServiceIml(PostRepository postRepository,
                          TagRepository tagRepository,
                          CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public PostListResponse getPosts(String search, int pageNumber, int pageSize) {
        SearchCriteria criteria = parseSearchString(search);

        int offset = (pageNumber - 1) * pageSize;

        List<PostEntity> posts = postRepository.findPostsWithFilters(
                criteria.getTitleSubstring(),
                criteria.getTags(),
                pageSize,
                offset
        );

        int totalPosts = postRepository.countPostsWithFilters(
                criteria.getTitleSubstring(),
                criteria.getTags()
        );

        List<PostPreview> postDtos = posts.stream()
                .map(this::convertToPreviewDto)
                .toList();

        int lastPage = (int) Math.ceil((double) totalPosts / pageSize);
        boolean hasPrev = pageNumber > 1;
        boolean hasNext = pageNumber < lastPage;

        return PostListResponse.builder()
                .posts(postDtos)
                .hasPrev(hasPrev)
                .hasNext(hasNext)
                .lastPage(lastPage)
                .build();
    }

    @Override
    public PostResponse getPostById(Long postId) {
        PostEntity post = postRepository.findById(postId);
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .text(post.getText())
                .likesCount(post.getLikesCount())
                .tags(post.getTags().stream().map(TagEntity::getName).toList())
                .commentsCount(commentRepository.countCommentsByPost(postId))
                .build();

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
        PostEntity postEntity = postRepository.findById(request.getId());

        postEntity.setTitle(request.getTitle());
        postEntity.setText(request.getText());

        if (!request.getTags().isEmpty()) {
            tagRepository.deleteTagAndPost(request.getId());
            List<TagEntity> tagEntities = tagRepository.getTags(request.getTags());
            postEntity.setTags(tagEntities);
        }

        postRepository.update(postEntity);
        return PostResponse.builder()
                .id(postEntity.getId())
                .title(postEntity.getTitle())
                .text(postEntity.getText())
                .likesCount(postEntity.getLikesCount())
                .tags(postEntity.getTags().stream().map(TagEntity::getName).toList())
                .commentsCount(commentRepository.countCommentsByPost(postEntity.getId()))
                .image(postEntity.getImage())
                .build();
    }

    @Override
    public void deletePost(Long postId) {
        commentRepository.deleteByPostId(postId);
        tagRepository.deleteTagAndPost(postId);
        postRepository.delete(postId);
    }

    @Override
    public Long incrementLikes(Long id) {
        return 0L;
    }

    @Override
    public void updatePostImage(Long postId, byte[] image) {
        PostEntity postEntity = postRepository.findById(postId);
        postEntity.setImage(image);
        postRepository.update(postEntity);
    }

    @Override
    public byte[] getPostImage(Long postId) {
        PostEntity postEntity = postRepository.findById(postId);
        return postEntity.getImage();
    }


    private SearchCriteria parseSearchString(String search) {
        if (search == null || search.trim().isEmpty()) {
            return new SearchCriteria("", Collections.emptyList());
        }

        String[] words = search.trim().split("\\s+");

        List<String> tags = new ArrayList<>();
        List<String> titleWords = new ArrayList<>();

        for (String word : words) {
            if (word.startsWith("#")) {
                String tag = word.substring(1);
                if (!tag.isEmpty()) {
                    tags.add(tag);
                }
            } else if (!word.isEmpty()) {
                titleWords.add(word);
            }
        }

        String titleSubstring = String.join(" ", titleWords);

        return new SearchCriteria(titleSubstring, tags);
    }

    private PostPreview convertToPreviewDto(PostEntity post) {
        String truncatedText = post.getText();
        if (truncatedText.length() > 128) {
            truncatedText = truncatedText.substring(0, 128) + "...";
        }

        return PostPreview.builder()
                .id(post.getId())
                .title(post.getTitle())
                .text(truncatedText)
                .tags(post.getTags().stream().map(TagEntity::getName).toList())
                .likesCount(post.getLikesCount())
                .commentsCount(commentRepository.countCommentsByPost(post.getId()))
                .build();
    }

}
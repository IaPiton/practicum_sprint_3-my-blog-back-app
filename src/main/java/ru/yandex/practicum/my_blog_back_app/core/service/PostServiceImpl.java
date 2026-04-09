package ru.yandex.practicum.my_blog_back_app.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.PostCreateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.PostUpdateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostListResponse;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostPreview;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostResponse;
import ru.yandex.practicum.my_blog_back_app.api.handler.EntityNotFoundException;
import ru.yandex.practicum.my_blog_back_app.core.model.SearchCriteria;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.PostEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.TagEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.mapper.PostMapper;
import ru.yandex.practicum.my_blog_back_app.persistence.repository.PostRepository;
import ru.yandex.practicum.my_blog_back_app.persistence.repository.TagRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostMapper postMapper;

    @Override
    public PostListResponse getPosts(String search, int pageNumber, int pageSize) {

        SearchCriteria criteria = parseSearchString(search);
        int offset = (pageNumber - 1) * pageSize;
        List<PostEntity> posts;

        if (criteria.getTags() == null || criteria.getTags().isEmpty()) {
            posts = postRepository.findPostsWithFiltersNoTags(criteria.getTitleSubstring(), offset, pageSize);
        } else {
            posts = postRepository.findPostsWithFiltersWithTags(criteria.getTitleSubstring(), criteria.getTags(), offset, pageSize);
        }


        int totalPosts;
        if (criteria.getTags() == null || criteria.getTags().isEmpty()) {
            totalPosts = postRepository.findCountPostsWithFiltersNoTags(criteria.getTitleSubstring());
        } else {
            totalPosts = postRepository.findCountPostsWithFiltersWithTags(criteria.getTitleSubstring(), criteria.getTags());
        }

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
        return postMapper.toResponse(postRepository.findById(postId)
                .orElseThrow(EntityNotFoundException::new));

    }

    @Override
    public PostResponse createPost(PostCreateRequest request) {
        Set<TagEntity> tags = request.getTags().stream()
                .map(tag -> tagRepository.findByName(tag)
                        .orElseGet(() -> tagRepository.save(new TagEntity(null, tag, null))))
                .collect(Collectors.toSet());

        PostEntity postEntity = postMapper.toEntity(request);
        postEntity.setTags(tags);
        postEntity = postRepository.save(postEntity);

        return postMapper.toResponse(postEntity);
    }

    @Override
    public PostResponse updatePost(PostUpdateRequest request) {
        PostEntity postEntity = postRepository.findById(request.getId()).orElseThrow(EntityNotFoundException::new);
        Set<TagEntity> tags;
        if (request.getTags() != null) {
            tags = request.getTags().stream()
                    .map(tag -> tagRepository.findByName(tag)
                            .orElseGet(() -> tagRepository.save(new TagEntity(null, tag, null))))
                    .collect(Collectors.toSet());
            postEntity.setTags(tags);
        }
        postEntity.setTitle(request.getTitle());
        postEntity.setText(request.getText());
        postEntity = postRepository.save(postEntity);
        return postMapper.toResponse(postEntity);

    }

    @Override
    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }

    @Override
    public Long incrementLikes(Long postId) {
        PostEntity postEntity = postRepository.findById(postId).orElseThrow(EntityNotFoundException::new);
        postEntity.setLikesCount(postEntity.getLikesCount() + 1);
        postEntity = postRepository.save(postEntity);
        return postEntity.getLikesCount();
    }

    @Override
    public void updatePostImage(Long postId, byte[] image) {
        PostEntity postEntity = postRepository.findById(postId).orElseThrow(EntityNotFoundException::new);
        postEntity.setImage(image);
        postRepository.save(postEntity);
    }

    @Override
    public byte[] getPostImage(Long postId) {
        PostEntity postEntity = postRepository.findById(postId).orElseThrow(EntityNotFoundException::new);
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

        return postMapper.toPreviewResponse(post, truncatedText);
    }

}
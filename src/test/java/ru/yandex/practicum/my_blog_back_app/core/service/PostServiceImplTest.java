package ru.yandex.practicum.my_blog_back_app.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.PostCreateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.PostUpdateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostListResponse;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostResponse;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostPreview;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.PostEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.TagEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.mapper.PostMapper;
import ru.yandex.practicum.my_blog_back_app.persistence.repository.CommentRepository;
import ru.yandex.practicum.my_blog_back_app.persistence.repository.PostRepository;
import ru.yandex.practicum.my_blog_back_app.persistence.repository.TagRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса постов")
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostServiceImpl postService;

    private PostEntity testPostEntity;
    private PostResponse testPostResponse;
    private PostPreview testPostPreview;

    @BeforeEach
    void setUp() {
        testPostEntity = new PostEntity();
        testPostEntity.setId(1L);
        testPostEntity.setTitle("Тестовый пост");
        testPostEntity.setText("Очень длинное содержание поста, которое превышает сто двадцать восемь символов для проверки обрезки текста в превью.");
        testPostEntity.setLikesCount(5L);

        testPostResponse = PostResponse.builder()
                .id(1L)
                .title("Тестовый пост")
                .text("Содержание")
                .likesCount(5L)
                .build();

        testPostPreview = PostPreview.builder()
                .id(1L)
                .title("Тестовый пост")
                .text("Очень длинное содержание поста, которое превышает сто двадцать восемь символов для проверки обрезки текста в превью.")
                .likesCount(5L)
                .build();
    }

    @Nested
    @DisplayName("Тесты получения списка постов")
    class GetPostsTests {

        @Test
        @DisplayName("Должен вернуть список постов без поискового запроса")
        void shouldReturnPostsWithoutSearch() {
            List<PostEntity> posts = List.of(testPostEntity);
            when(postRepository.findPostsWithFilters(eq(""), eq(List.of()), eq(10), eq(0)))
                    .thenReturn(posts);
            when(postRepository.countPostsWithFilters(eq(""), eq(List.of())))
                    .thenReturn(1);
            when(postMapper.toPreviewResponse(any(PostEntity.class), anyString()))
                    .thenReturn(testPostPreview);

            PostListResponse response = postService.getPosts(null, 1, 10);

            assertThat(response).isNotNull();
            assertThat(response.getPosts()).hasSize(1);
            assertThat(response.getHasPrev()).isFalse();
            assertThat(response.getHasNext()).isFalse();
            assertThat(response.getLastPage()).isEqualTo(1);
        }

        @Test
        @DisplayName("Должен корректно распарсить поиск по тегам и заголовку")
        void shouldParseSearchWithTagsAndTitle() {
            String searchQuery = "привет мир #java #spring";
            List<PostEntity> posts = List.of(testPostEntity);
            when(postRepository.findPostsWithFilters(eq("привет мир"), eq(List.of("java", "spring")), eq(10), eq(0)))
                    .thenReturn(posts);
            when(postRepository.countPostsWithFilters(eq("привет мир"), eq(List.of("java", "spring"))))
                    .thenReturn(1);
            when(postMapper.toPreviewResponse(any(), anyString())).thenReturn(testPostPreview);

            PostListResponse response = postService.getPosts(searchQuery, 1, 10);

            assertThat(response.getPosts()).hasSize(1);
        }

        @Test
        @DisplayName("Должен корректно вычислить пагинацию для нескольких страниц")
        void shouldCalculatePaginationCorrectly() {
            List<PostEntity> posts = List.of(testPostEntity, testPostEntity);
            when(postRepository.findPostsWithFilters(any(), any(), eq(2), eq(0)))
                    .thenReturn(posts);
            when(postRepository.countPostsWithFilters(any(), any()))
                    .thenReturn(5);
            when(postMapper.toPreviewResponse(any(), anyString())).thenReturn(testPostPreview);

            PostListResponse response = postService.getPosts(null, 1, 2);

            assertThat(response.getLastPage()).isEqualTo(3);
            assertThat(response.getHasPrev()).isFalse();
            assertThat(response.getHasNext()).isTrue();
        }
    }

    @Nested
    @DisplayName("Тесты получения поста по ID")
    class GetPostByIdTests {

        @Test
        @DisplayName("Должен вернуть пост, если он существует")
        void shouldReturnPostWhenExists() {
            when(postRepository.findById(1L)).thenReturn(Optional.of(testPostEntity));
            when(postMapper.toResponse(testPostEntity)).thenReturn(testPostResponse);

            PostResponse result = postService.getPostById(1L);

            assertThat(result).isEqualTo(testPostResponse);
        }

        @Test
        @DisplayName("Должен вернуть пустой пост, если пост не найден")
        void shouldReturnEmptyPostWhenNotFound() {
            when(postRepository.findById(99L)).thenReturn(Optional.empty());
            when(postMapper.toResponse(any(PostEntity.class))).thenReturn(
                    PostResponse.builder().build()
            );

            PostResponse result = postService.getPostById(99L);

            assertThat(result.getId()).isNull();
        }
    }

    @Nested
    @DisplayName("Тесты создания поста")
    class CreatePostTests {

        @Test
        @DisplayName("Должен успешно создать пост с тегами")
        void shouldCreatePostWithTags() {
            PostCreateRequest request = PostCreateRequest.builder()
                    .title("Новый пост")
                    .text("Содержание")
                    .tags(List.of("java", "spring"))
                    .build();

            List<TagEntity> tags = List.of(new TagEntity(), new TagEntity());
            when(tagRepository.getTags(List.of("java", "spring"))).thenReturn(tags);
            when(postMapper.toEntity(request, tags)).thenReturn(testPostEntity);
            when(postRepository.savePost(testPostEntity)).thenReturn(testPostEntity);
            when(postMapper.toResponse(testPostEntity)).thenReturn(testPostResponse);

            PostResponse result = postService.createPost(request);

            assertThat(result).isEqualTo(testPostResponse);
            verify(tagRepository).getTags(List.of("java", "spring"));
            verify(postRepository).savePost(testPostEntity);
        }
    }

    @Nested
    @DisplayName("Тесты обновления поста")
    class UpdatePostTests {

        @Test
        @DisplayName("Должен обновить пост без изменения тегов")
        void shouldUpdatePostWithoutTags() {

            PostUpdateRequest request = PostUpdateRequest.builder()
                    .id(1L)
                    .title("Обновленный заголовок")
                    .text("Обновленный текст")
                    .tags(List.of())
                    .build();

            when(postRepository.findById(1L)).thenReturn(Optional.of(testPostEntity));
            when(postMapper.toResponse(any(PostEntity.class))).thenReturn(testPostResponse);

            postService.updatePost(request);

            assertThat(testPostEntity.getTitle()).isEqualTo("Обновленный заголовок");
            assertThat(testPostEntity.getText()).isEqualTo("Обновленный текст");
            verify(tagRepository, never()).deleteTagAndPost(anyLong());
            verify(postRepository).update(testPostEntity);
        }

        @Test
        @DisplayName("Должен обновить пост с новыми тегами")
        void shouldUpdatePostWithNewTags() {
            PostUpdateRequest request = PostUpdateRequest.builder()
                    .id(1L)
                    .title("Обновленный заголовок")
                    .text("Обновленный текст")
                    .tags(List.of("newTag"))
                    .build();

            List<TagEntity> newTags = List.of(new TagEntity());
            when(postRepository.findById(1L)).thenReturn(Optional.of(testPostEntity));
            when(tagRepository.getTags(List.of("newTag"))).thenReturn(newTags);
            when(postMapper.toResponse(any(PostEntity.class))).thenReturn(testPostResponse);

            postService.updatePost(request);

            verify(tagRepository).deleteTagAndPost(1L);
            verify(tagRepository).getTags(List.of("newTag"));
            assertThat(testPostEntity.getTags()).isEqualTo(newTags);
        }

        @Test
        @DisplayName("При обновлении несуществующего поста должен создать новый")
        void shouldCreateNewPostIfNotFoundOnUpdate() {
            PostUpdateRequest request = PostUpdateRequest.builder()
                    .id(99L)
                    .title("Заголовок")
                    .text("Текст")
                    .tags(List.of())
                    .build();

            when(postRepository.findById(99L)).thenReturn(Optional.empty());
            when(postMapper.toResponse(any(PostEntity.class))).thenReturn(testPostResponse);

            postService.updatePost(request);

            verify(postRepository).update(any(PostEntity.class));
        }
    }

    @Nested
    @DisplayName("Тесты удаления поста")
    class DeletePostTests {

        @Test
        @DisplayName("Должен удалить пост и связанные сущности")
        void shouldDeletePostAndRelatedEntities() {
            Long postId = 1L;

            postService.deletePost(postId);

            verify(commentRepository).deleteByPostId(postId);
            verify(tagRepository).deleteTagAndPost(postId);
            verify(postRepository).delete(postId);
        }
    }

    @Nested
    @DisplayName("Тесты лайков")
    class LikesTests {

        @Test
        @DisplayName("Должен увеличить количество лайков и вернуть новое значение")
        void shouldIncrementLikesAndReturnNewCount() {
            when(postRepository.findById(1L)).thenReturn(Optional.of(testPostEntity));

            Long newLikesCount = postService.incrementLikes(1L);

             assertThat(newLikesCount).isEqualTo(6L);
            assertThat(testPostEntity.getLikesCount()).isEqualTo(6L);
            verify(postRepository).update(testPostEntity);
        }

        @Test
        @DisplayName("При лайке несуществующего поста должен вернуть 1")
        void shouldReturnOneIfPostNotFoundOnLike() {
            when(postRepository.findById(99L)).thenReturn(Optional.empty());

            Long newLikesCount = postService.incrementLikes(99L);

            assertThat(newLikesCount).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("Тесты работы с изображениями")
    class ImageTests {

        @Test
        @DisplayName("Должен обновить изображение поста")
        void shouldUpdatePostImage() {
            byte[] imageData = new byte[]{1, 2, 3};
            when(postRepository.findById(1L)).thenReturn(Optional.of(testPostEntity));

            postService.updatePostImage(1L, imageData);

            assertThat(testPostEntity.getImage()).isEqualTo(imageData);
            verify(postRepository).update(testPostEntity);
        }

        @Test
        @DisplayName("Должен вернуть изображение поста")
        void shouldGetPostImage() {
            byte[] imageData = new byte[]{1, 2, 3};
            testPostEntity.setImage(imageData);
            when(postRepository.findById(1L)).thenReturn(Optional.of(testPostEntity));

            byte[] result = postService.getPostImage(1L);

            assertThat(result).isEqualTo(imageData);
        }

        @Test
        @DisplayName("При отсутствии поста должен вернуть null изображение")
        void shouldReturnNullImageIfPostNotFound() {
            when(postRepository.findById(99L)).thenReturn(Optional.empty());

             byte[] result = postService.getPostImage(99L);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Тесты приватных методов")
    class PrivateMethodTests {

        @Test
        @DisplayName("Должен обрезать длинный текст в превью до 128 символов")
        void shouldTruncateLongTextInPreview() {
            String longText = "a".repeat(200);
            testPostEntity.setText(longText);
            List<PostEntity> posts = List.of(testPostEntity);

            when(postRepository.findPostsWithFilters(any(), any(), eq(10), eq(0)))
                    .thenReturn(posts);
            when(postRepository.countPostsWithFilters(any(), any()))
                    .thenReturn(1);
            when(postMapper.toPreviewResponse(eq(testPostEntity), anyString()))
                    .thenAnswer(invocation -> {
                        String truncated = invocation.getArgument(1);
                        assertThat(truncated).hasSize(128 + 3); // + "..."
                        assertThat(truncated).endsWith("...");
                        return testPostPreview;
                    });

            postService.getPosts(null, 1, 10);

            verify(postMapper).toPreviewResponse(eq(testPostEntity), argThat(text -> text.length() == 131));
        }
    }
}
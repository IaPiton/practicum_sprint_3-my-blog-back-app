package ru.yandex.practicum.my_blog_back_app.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.PostCreateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.PostUpdateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostListResponse;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostPreview;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostResponse;
import ru.yandex.practicum.my_blog_back_app.api.handler.EntityNotFoundException;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.PostEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.TagEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.repository.PostRepository;
import ru.yandex.practicum.my_blog_back_app.persistence.repository.TagRepository;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Интеграционные тесты сервиса постов с реальной БД")
class PostServiceImplTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    private PostEntity testPost;

    @BeforeEach
    void setUp() {
        TagEntity testTag1 = new TagEntity(null, "spring", null);
        TagEntity testTag2 = new TagEntity(null, "java", null);
        testTag1 = tagRepository.save(testTag1);
        testTag2 = tagRepository.save(testTag2);

        testPost = new PostEntity();
        testPost.setTitle("Тестовый пост о Spring");
        testPost.setText("Это содержимое тестового поста. Оно достаточно длинное для проверки обрезания текста в превью. " +
                "Добавим еще текста, чтобы точно превысить лимит в 128 символов. " +
                "Вот теперь точно хватит для проверки.");
        testPost.setLikesCount(0L);
        testPost.setTags(Set.of(testTag1, testTag2));
        testPost = postRepository.save(testPost);
    }

    @Nested
    @DisplayName("Тесты получения списка постов")
    class GetPostsTests {

        @Test
        @DisplayName("Должен вернуть список постов с пагинацией")
        void shouldReturnPostsWithPagination() {
            for (int i = 1; i <= 5; i++) {
                PostEntity post = new PostEntity();
                post.setTitle("Пост " + i);
                post.setText("Содержимое поста " + i);
                post.setLikesCount(0L);
                postRepository.save(post);
            }

            PostListResponse result = postService.getPosts(null, 1, 3);

            assertThat(result).isNotNull();
            assertThat(result.getPosts()).hasSize(3);
            assertThat(result.getHasPrev()).isFalse();
            assertThat(result.getHasNext()).isTrue();
            assertThat(result.getLastPage()).isGreaterThan(1);
        }

        @Test
        @DisplayName("Должен вернуть посты с поиском по заголовку")
        void shouldReturnPostsFilteredByTitle() {
            PostListResponse result = postService.getPosts("Spring", 1, 10);

            assertThat(result).isNotNull();
            assertThat(result.getPosts()).isNotEmpty();
            assertThat(result.getPosts().get(0).getTitle()).contains("Spring");
        }

        @Test
        @DisplayName("Должен вернуть посты с поиском по тегам")
        void shouldReturnPostsFilteredByTags() {
            PostListResponse result = postService.getPosts("#spring", 1, 10);

            assertThat(result).isNotNull();
            assertThat(result.getPosts()).isNotEmpty();
        }

        @Test
        @DisplayName("Должен вернуть посты с поиском по заголовку и тегам одновременно")
        void shouldReturnPostsFilteredByTitleAndTags() {
            PostListResponse result = postService.getPosts("Spring #java", 1, 10);

            assertThat(result).isNotNull();
            assertThat(result.getPosts()).isNotEmpty();
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если посты не найдены")
        void shouldReturnEmptyListWhenNoPostsFound() {
            PostListResponse result = postService.getPosts("несуществующийпост", 1, 10);

            assertThat(result).isNotNull();
            assertThat(result.getPosts()).isEmpty();
            assertThat(result.getHasPrev()).isFalse();
            assertThat(result.getHasNext()).isFalse();
            assertThat(result.getLastPage()).isEqualTo(0);
        }

        @Test
        @DisplayName("Должен корректно обрезать текст в превью до 128 символов")
        void shouldTruncateTextInPreview() {
            PostListResponse result = postService.getPosts(null, 1, 10);

            assertThat(result).isNotNull();
            assertThat(result.getPosts()).isNotEmpty();

            PostPreview preview = result.getPosts().get(0);
            assertThat(preview.getText()).isNotNull();
            assertThat(preview.getText().length()).isLessThanOrEqualTo(131);
        }
    }

    @Nested
    @DisplayName("Тесты получения поста по ID")
    class GetPostByIdTests {

        @Test
        @DisplayName("Должен вернуть пост по существующему ID")
        void shouldReturnPostById() {
            PostResponse result = postService.getPostById(testPost.getId());

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testPost.getId());
            assertThat(result.getTitle()).isEqualTo("Тестовый пост о Spring");
            assertThat(result.getTags()).containsExactlyInAnyOrder("spring", "java");
        }

        @Test
        @DisplayName("Должен выбросить исключение, если пост не найден")
        void shouldThrowExceptionWhenPostNotFound() {
            assertThatThrownBy(() -> postService.getPostById(99999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Тесты создания поста")
    class CreatePostTests {

        @Test
        @DisplayName("Должен успешно создать новый пост с тегами")
        void shouldCreatePostSuccessfully() {
            PostCreateRequest request = PostCreateRequest.builder()
                    .title("Новый пост")
                    .text("Содержимое нового поста")
                    .tags(List.of("spring", "newtag", "test"))
                    .build();

            PostResponse result = postService.createPost(request);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Новый пост");
            assertThat(result.getText()).isEqualTo("Содержимое нового поста");
            assertThat(result.getTags()).containsExactlyInAnyOrder("spring", "newtag", "test");

             PostEntity savedPost = postRepository.findById(result.getId()).orElse(null);
            assertThat(savedPost).isNotNull();
            assertThat(savedPost.getTitle()).isEqualTo("Новый пост");

            assertThat(tagRepository.findByName("newtag")).isPresent();
            assertThat(tagRepository.findByName("test")).isPresent();
        }

        @Test
        @DisplayName("Должен создать пост без тегов")
        void shouldCreatePostWithoutTags() {
            PostCreateRequest request = PostCreateRequest.builder()
                    .title("Пост без тегов")
                    .text("Содержимое поста без тегов")
                    .tags(List.of())
                    .build();

            PostResponse result = postService.createPost(request);

            assertThat(result).isNotNull();
            assertThat(result.getTags()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Тесты обновления поста")
    class UpdatePostTests {

        @Test
        @DisplayName("Должен успешно обновить существующий пост")
        void shouldUpdatePostSuccessfully() {
            PostUpdateRequest request = PostUpdateRequest.builder()
                    .id(testPost.getId())
                    .title("Обновленный заголовок")
                    .text("Обновленное содержимое")
                    .tags(List.of("updated", "java"))
                    .build();

            PostResponse result = postService.updatePost(request);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testPost.getId());
            assertThat(result.getTitle()).isEqualTo("Обновленный заголовок");
            assertThat(result.getText()).isEqualTo("Обновленное содержимое");
            assertThat(result.getTags()).containsExactlyInAnyOrder("updated", "java");

            PostEntity updatedPost = postRepository.findById(testPost.getId()).orElse(null);
            assertThat(updatedPost).isNotNull();
            assertThat(updatedPost.getTitle()).isEqualTo("Обновленный заголовок");
            assertThat(updatedPost.getText()).isEqualTo("Обновленное содержимое");
        }

        @Test
        @DisplayName("Должен выбросить исключение при обновлении несуществующего поста")
        void shouldThrowExceptionWhenUpdatingNonExistentPost() {
            PostUpdateRequest request = PostUpdateRequest.builder()
                    .id(99999L)
                    .title("Несуществующий пост")
                    .text("Содержимое")
                    .tags(List.of())
                    .build();

            assertThatThrownBy(() -> postService.updatePost(request))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("Должен обновить только заголовок и текст, сохранив старые теги если не указаны новые")
        void shouldUpdateOnlyTitleAndText() {
            PostUpdateRequest request = PostUpdateRequest.builder()
                    .id(testPost.getId())
                    .title("Только заголовок обновлен")
                    .text("Только текст обновлен")
                    .tags(null)
                    .build();

            PostResponse result = postService.updatePost(request);

            assertThat(result.getTitle()).isEqualTo("Только заголовок обновлен");
            assertThat(result.getText()).isEqualTo("Только текст обновлен");
            assertThat(result.getTags()).containsExactlyInAnyOrder("spring", "java");
        }
    }

    @Nested
    @DisplayName("Тесты удаления поста")
    class DeletePostTests {

        @Test
        @DisplayName("Должен успешно удалить пост по ID")
        void shouldDeletePostById() {
            postService.deletePost(testPost.getId());

            boolean exists = postRepository.existsById(testPost.getId());
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("При удалении несуществующего поста не должно быть ошибки")
        void shouldNotThrowErrorWhenDeletingNonExistentPost() {
            postService.deletePost(99999L);
        }

        @Test
        @DisplayName("Должен корректно удалить несколько постов подряд")
        void shouldDeleteMultiplePostsInRow() {
            PostEntity post2 = new PostEntity();
            post2.setTitle("Второй пост");
            post2.setText("Содержимое");
            post2.setLikesCount(0L);
            post2 = postRepository.save(post2);

            PostEntity post3 = new PostEntity();
            post3.setTitle("Третий пост");
            post3.setText("Содержимое");
            post3.setLikesCount(0L);
            post3 = postRepository.save(post3);

            postService.deletePost(testPost.getId());
            postService.deletePost(post2.getId());
            postService.deletePost(post3.getId());

            assertThat(postRepository.existsById(testPost.getId())).isFalse();
            assertThat(postRepository.existsById(post2.getId())).isFalse();
            assertThat(postRepository.existsById(post3.getId())).isFalse();
        }
    }

    @Nested
    @DisplayName("Тесты лайков")
    class LikesTests {

        @Test
        @DisplayName("Должен увеличить счетчик лайков на 1")
        void shouldIncrementLikesCount() {
            Long initialLikes = testPost.getLikesCount();

            Long newLikesCount = postService.incrementLikes(testPost.getId());

            assertThat(newLikesCount).isEqualTo(initialLikes + 1);

            PostEntity updatedPost = postRepository.findById(testPost.getId()).orElse(null);
            assertThat(updatedPost).isNotNull();
            assertThat(updatedPost.getLikesCount()).isEqualTo(initialLikes + 1);
        }

        @Test
        @DisplayName("Должен выбросить исключение при попытке лайкнуть несуществующий пост")
        void shouldThrowExceptionWhenLikingNonExistentPost() {
            assertThatThrownBy(() -> postService.incrementLikes(99999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("Должен корректно увеличивать лайки несколько раз")
        void shouldIncrementLikesMultipleTimes() {
            postService.incrementLikes(testPost.getId());
            postService.incrementLikes(testPost.getId());
            Long result = postService.incrementLikes(testPost.getId());

            assertThat(result).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("Тесты работы с изображениями")
    class ImageTests {

        @Test
        @DisplayName("Должен обновить изображение поста")
        void shouldUpdatePostImage() {
            byte[] testImage = "test image content".getBytes();

            postService.updatePostImage(testPost.getId(), testImage);

            byte[] retrievedImage = postService.getPostImage(testPost.getId());
            assertThat(retrievedImage).isEqualTo(testImage);
        }

        @Test
        @DisplayName("Должен вернуть null если изображение не установлено")
        void shouldReturnNullWhenImageNotSet() {
            byte[] image = postService.getPostImage(testPost.getId());

            assertThat(image).isNull();
        }

        @Test
        @DisplayName("Должен выбросить исключение при обновлении изображения у несуществующего поста")
        void shouldThrowExceptionWhenUpdatingImageForNonExistentPost() {
            byte[] testImage = "test".getBytes();

            assertThatThrownBy(() -> postService.updatePostImage(99999L, testImage))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("Должен выбросить исключение при получении изображения несуществующего поста")
        void shouldThrowExceptionWhenGettingImageForNonExistentPost() {
            assertThatThrownBy(() -> postService.getPostImage(99999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("Должен обновить изображение несколько раз")
        void shouldUpdateImageMultipleTimes() {
            byte[] firstImage = "first".getBytes();
            byte[] secondImage = "second".getBytes();

            postService.updatePostImage(testPost.getId(), firstImage);
            byte[] retrievedFirst = postService.getPostImage(testPost.getId());
            assertThat(retrievedFirst).isEqualTo(firstImage);

            postService.updatePostImage(testPost.getId(), secondImage);
            byte[] retrievedSecond = postService.getPostImage(testPost.getId());
            assertThat(retrievedSecond).isEqualTo(secondImage);
        }
    }

    @Nested
    @DisplayName("Интеграционные тесты потока данных")
    class IntegrationFlowTests {

        @Test
        @DisplayName("Должен корректно обработать полный цикл CRUD операций")
        void shouldHandleFullCrudCycle() {
            PostCreateRequest createRequest = PostCreateRequest.builder()
                    .title("CRUD Тест пост")
                    .text("Содержимое CRUD теста")
                    .tags(List.of("crud", "test"))
                    .build();

            PostResponse created = postService.createPost(createRequest);
            assertThat(created).isNotNull();
            Long postId = created.getId();

            PostResponse found = postService.getPostById(postId);
            assertThat(found.getTitle()).isEqualTo("CRUD Тест пост");

            PostUpdateRequest updateRequest = PostUpdateRequest.builder()
                    .id(postId)
                    .title("Обновленный CRUD пост")
                    .text("Обновленное содержимое")
                    .tags(List.of("updated"))
                    .build();

            PostResponse updated = postService.updatePost(updateRequest);
            assertThat(updated.getTitle()).isEqualTo("Обновленный CRUD пост");

            Long likes = postService.incrementLikes(postId);
            assertThat(likes).isEqualTo(1L);

            postService.deletePost(postId);

            assertThatThrownBy(() -> postService.getPostById(postId))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("Должен корректно обработать поиск с разными комбинациями параметров")
        void shouldHandleDifferentSearchCombinations() {
             PostCreateRequest post1 = PostCreateRequest.builder()
                    .title("Java пост")
                    .text("О Java")
                    .tags(List.of("java"))
                    .build();

            PostCreateRequest post2 = PostCreateRequest.builder()
                    .title("Spring пост")
                    .text("О Spring")
                    .tags(List.of("spring"))
                    .build();

            PostCreateRequest post3 = PostCreateRequest.builder()
                    .title("Java и Spring")
                    .text("О Java и Spring")
                    .tags(List.of("java", "spring"))
                    .build();

            postService.createPost(post1);
            postService.createPost(post2);
            postService.createPost(post3);

            PostListResponse titleSearch = postService.getPosts("Java", 1, 10);
            assertThat(titleSearch.getPosts()).allMatch(p -> p.getTitle().contains("Java"));

            PostListResponse tagSearch = postService.getPosts("#spring", 1, 10);
            assertThat(tagSearch.getPosts()).isNotEmpty();

            PostListResponse combinedSearch = postService.getPosts("Java #spring", 1, 10);
            assertThat(combinedSearch.getPosts()).isNotEmpty();
        }
    }
}
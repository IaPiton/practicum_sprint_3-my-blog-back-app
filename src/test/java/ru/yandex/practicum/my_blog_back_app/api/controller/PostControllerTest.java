package ru.yandex.practicum.my_blog_back_app.api.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.PostCreateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.PostUpdateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostListResponse;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostPreview;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostResponse;
import ru.yandex.practicum.my_blog_back_app.api.handler.InvalidImageException;
import ru.yandex.practicum.my_blog_back_app.core.service.PostService;
import ru.yandex.practicum.my_blog_back_app.core.validator.ImageValidator;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты PostController")
class PostControllerTest {

    @Mock
    private PostService postService;

    @Mock
    private ImageValidator imageValidator;

    @InjectMocks
    private PostController postController;

    private static final Long VALID_POST_ID = 1L;
    private static final String SEARCH_QUERY = "test";
    private static final byte[] TEST_IMAGE_BYTES = new byte[]{1, 2, 3, 4, 5};

    private static PostResponse samplePostResponse;
    private static PostCreateRequest sampleCreateRequest;
    private static PostUpdateRequest sampleUpdateRequest;
    private static PostListResponse samplePostListResponse;

    @BeforeAll
    static void setUp() {
        samplePostResponse = PostResponse.builder()
                .id(VALID_POST_ID)
                .title("Заголовок")
                .text("Текст")
                .tags(List.of("Tag_1", "Tag_2"))
                .likesCount(10L)
                .build();

        sampleCreateRequest = PostCreateRequest.builder()
                .title("Заголовок")
                .text("Текст")
                .tags(List.of("Tag_1", "Tag_2"))
                .build();

        sampleUpdateRequest = PostUpdateRequest.builder()
                .id(VALID_POST_ID)
                .title("Заголовок")
                .text("Текст")
                .tags(List.of("Tag_1", "Tag_2"))
                .build();

        PostPreview postPreview = PostPreview.builder()
                .id(VALID_POST_ID)
                .title("Заголовок")
                .text("Текст")
                .tags(List.of("Tag_1", "Tag_2"))
                .likesCount(10L)
                .build();

        samplePostListResponse = PostListResponse.builder()
                .posts(List.of(postPreview))
                .hasPrev(true)
                .hasNext(true)
                .lastPage(10)
                .build();
    }

    @Nested
    @DisplayName("Тесты для getPosts()")
    class GetPostsTests {

        @Test
        @DisplayName("Возвращает список постов")
        void shouldReturnPostsWithDefaultParameters() {
            when(postService.getPosts("", 0, 5)).thenReturn(samplePostListResponse);

            ResponseEntity<PostListResponse> response = postController.getPosts("", 0, 5);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPosts()).hasSize(1);
            assertThat(response.getBody().getLastPage()).isEqualTo(10);

            verify(postService).getPosts("", 0, 5);
        }

        @Test
        @DisplayName("Корректно отработает поисковый запрос")
        void shouldHandleSearchQuery() {
            when(postService.getPosts(SEARCH_QUERY, 0, 5)).thenReturn(samplePostListResponse);

            ResponseEntity<PostListResponse> response = postController.getPosts(SEARCH_QUERY, 0, 5);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            verify(postService).getPosts(SEARCH_QUERY, 0, 5);
        }

        @Test
        @DisplayName("Отредактирует отрицательный номер страницы на 0")
        void shouldCorrectNegativePageNumber() {
            int negativePageNumber = -1;
            when(postService.getPosts("", 0, 5)).thenReturn(samplePostListResponse);

            ResponseEntity<PostListResponse> response = postController.getPosts("", negativePageNumber, 5);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(postService).getPosts("", 0, 5);
        }

        @ParameterizedTest
        @ValueSource(ints = {-5, -10, -1})
        @DisplayName("Отредактирует номера страниц на 0")
        void shouldCorrectAnyNegativePageNumber(int negativePageNumber) {
            when(postService.getPosts("", 0, 5)).thenReturn(samplePostListResponse);

            postController.getPosts("", negativePageNumber, 5);

            verify(postService).getPosts("", 0, 5);
        }

        @Test
        @DisplayName("Отредактирует размер страницы меньше 1 на 5")
        void shouldCorrectPageSizeLessThanOne() {
            int invalidPageSize = 0;
            when(postService.getPosts("", 0, 5)).thenReturn(samplePostListResponse);

            postController.getPosts("", 0, invalidPageSize);

            verify(postService).getPosts("", 0, 5);
        }

        @Test
        @DisplayName("Должен вернуть пустой список когда постов нет")
        void shouldReturnEmptyListWhenNoPosts() {
            PostListResponse emptyResponse = PostListResponse.builder()
                    .posts(List.of())
                    .hasNext(false)
                    .hasPrev(false)
                    .lastPage(0)
                    .build();
            when(postService.getPosts("", 0, 5)).thenReturn(emptyResponse);

            ResponseEntity<PostListResponse> response = postController.getPosts("", 0, 5);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            Assertions.assertNotNull(response.getBody());
            assertThat(response.getBody().getPosts()).isEmpty();

        }
    }

    @Nested
    @DisplayName("Тесты для getPostById()")
    class GetPostByIdTests {

        @Test
        @DisplayName("Вернет пост по id")
        void shouldReturnPostById() {
            when(postService.getPostById(VALID_POST_ID)).thenReturn(samplePostResponse);

            ResponseEntity<PostResponse> response = postController.getPostById(VALID_POST_ID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(VALID_POST_ID);
            assertThat(response.getBody().getTitle()).isEqualTo("Заголовок");

            verify(postService).getPostById(VALID_POST_ID);
        }

        @Test
        @DisplayName("Исключение когда пост не найден")
        void shouldThrowExceptionWhenPostNotFound() {
            Long nonExistentId = 999L;
            when(postService.getPostById(nonExistentId))
                    .thenThrow(new IllegalArgumentException("Пост не найден"));

            assertThatThrownBy(() -> postController.getPostById(nonExistentId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Пост не найден");

            verify(postService).getPostById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("Тесты для createPost()")
    class CreatePostTests {

        @Test
        @DisplayName("Создаст пост и вернет статус 201")
        void shouldCreatePostAndReturnCreatedStatus() {
            when(postService.createPost(sampleCreateRequest)).thenReturn(samplePostResponse);

            ResponseEntity<PostResponse> response = postController.createPost(sampleCreateRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(VALID_POST_ID);

            verify(postService).createPost(sampleCreateRequest);
        }
    }

    @Nested
    @DisplayName("Тесты для updatePost()")
    class UpdatePostTests {

        @Test
        @DisplayName("Обновит пост и вернет обновленные данные")
        void shouldUpdatePostAndReturnUpdatedData() {
            PostResponse updatedResponse = PostResponse.builder()
                    .id(VALID_POST_ID)
                    .title("Заголовок")
                    .text("Текст")
                    .build();

            when(postService.updatePost(sampleUpdateRequest)).thenReturn(updatedResponse);

            ResponseEntity<PostResponse> response = postController.updatePost(sampleUpdateRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Заголовок");

            verify(postService).updatePost(sampleUpdateRequest);
        }

        @Test
        @DisplayName("Исключение при обновлении несуществующего поста")
        void shouldThrowExceptionWhenUpdatingNonExistentPost() {
            PostUpdateRequest invalidRequest = PostUpdateRequest.builder()
                    .id(999L)
                    .title("Invalid Post")
                    .build();

            when(postService.updatePost(invalidRequest))
                    .thenThrow(new IllegalArgumentException("Пост не найден"));

            assertThatThrownBy(() -> postController.updatePost(invalidRequest))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Тесты для deletePost()")
    class DeletePostTests {

        @Test
        @DisplayName("Удалит пост и вернет статус 200")
        void shouldDeletePostAndReturnOk() {
            doNothing().when(postService).deletePost(VALID_POST_ID);

            ResponseEntity<Void> response = postController.deletePost(VALID_POST_ID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNull();

            verify(postService).deletePost(VALID_POST_ID);
        }

        @Test
        @DisplayName("Выбросит исключение при удалении несуществующего поста")
        void shouldThrowExceptionWhenDeletingNonExistentPost() {
            Long nonExistentId = 999L;
            doThrow(new IllegalArgumentException("Пост не найден"))
                    .when(postService).deletePost(nonExistentId);

             assertThatThrownBy(() -> postController.deletePost(nonExistentId))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(postService).deletePost(nonExistentId);
        }
    }

    @Nested
    @DisplayName("Тесты для incrementLikes()")
    class IncrementLikesTests {

        @Test
        @DisplayName("Увеличит количество лайков и вернет новое значение")
        void shouldIncrementLikesAndReturnNewCount() {
            Long newLikesCount = 11L;
            when(postService.incrementLikes(VALID_POST_ID)).thenReturn(newLikesCount);

            ResponseEntity<Long> response = postController.incrementLikes(VALID_POST_ID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(11L);

            verify(postService).incrementLikes(VALID_POST_ID);
        }

        @Test
        @DisplayName("Выбросит исключение при попытке увеличить лайки у несуществующего поста")
        void shouldThrowExceptionWhenIncrementingLikesForNonExistentPost() {
            Long nonExistentId = 999L;
            when(postService.incrementLikes(nonExistentId))
                    .thenThrow(new IllegalArgumentException("Пост не найден"));

            assertThatThrownBy(() -> postController.incrementLikes(nonExistentId))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Тесты для updatePostImage()")
    class UpdatePostImageTests {

        @Test
        @DisplayName("Обновит изображение поста при валидном файле")
        void shouldUpdatePostImageWhenValidFile()  {
            MultipartFile image = new MockMultipartFile(
                    "image",
                    "test.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    TEST_IMAGE_BYTES
            );

            doNothing().when(imageValidator).validate(image);
            doNothing().when(postService).updatePostImage(eq(VALID_POST_ID), any(byte[].class));

            ResponseEntity<Void> response = postController.updatePostImage(VALID_POST_ID, image);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            verify(imageValidator).validate(image);
            verify(postService).updatePostImage(eq(VALID_POST_ID), any(byte[].class));
        }

        @Test
        @DisplayName("Выбросит исключение при ошибке валидации изображения")
        void shouldThrowExceptionWhenImageValidationFails() {
            MultipartFile image = new MockMultipartFile(
                    "image",
                    "test.txt",
                    MediaType.TEXT_PLAIN_VALUE,
                    "not an image".getBytes()
            );

            doThrow(new InvalidImageException("Некорректный формат изображения"))
                    .when(imageValidator).validate(image);

            assertThatThrownBy(() -> postController.updatePostImage(VALID_POST_ID, image))
                    .isInstanceOf(InvalidImageException.class)
                    .hasMessage("Некорректный формат изображения");

            verify(imageValidator).validate(image);
            verify(postService, never()).updatePostImage(any(), any());
        }

        @Test
        @DisplayName("Выбросит исключение при пустом файле")
        void shouldThrowExceptionWhenFileIsEmpty() {
            MultipartFile image = new MockMultipartFile(
                    "image",
                    "empty.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    new byte[0]
            );

            doThrow(new InvalidImageException("Файл изображения пуст"))
                    .when(imageValidator).validate(image);

            assertThatThrownBy(() -> postController.updatePostImage(VALID_POST_ID, image))
                    .isInstanceOf(InvalidImageException.class);
        }

        @Test
        @DisplayName("Выбросит RuntimeException при ошибке чтения файла")
        void shouldThrowRuntimeExceptionWhenIOExceptionOccurs() throws IOException {
            MultipartFile image = mock(MultipartFile.class);
            when(image.getBytes()).thenThrow(new IOException("Ошибка чтения файла"));
            doNothing().when(imageValidator).validate(image);

            assertThatThrownBy(() -> postController.updatePostImage(VALID_POST_ID, image))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Ошибка при чтении файла");

            verify(imageValidator).validate(image);
            verify(postService, never()).updatePostImage(any(), any());
        }
    }

    @Nested
    @DisplayName("Тесты для getPostImage()")
    class GetPostImageTests {

        @Test
        @DisplayName("Вернет изображение когда оно существует")
        void shouldReturnImageWhenExists() {
            when(postService.getPostImage(VALID_POST_ID)).thenReturn(TEST_IMAGE_BYTES);

            ResponseEntity<byte[]> response = postController.getPostImage(VALID_POST_ID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEqualTo(TEST_IMAGE_BYTES);
            assertThat(response.getHeaders().getContentLength()).isEqualTo(TEST_IMAGE_BYTES.length);

            verify(postService).getPostImage(VALID_POST_ID);
        }

        @Test
        @DisplayName("Вернет пустой ответ когда изображение null")
        void shouldReturnEmptyResponseWhenImageIsNull() {
            when(postService.getPostImage(VALID_POST_ID)).thenReturn(null);

            ResponseEntity<byte[]> response = postController.getPostImage(VALID_POST_ID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNull();
            assertThat(response.getHeaders().getContentLength()).isZero();

            verify(postService).getPostImage(VALID_POST_ID);
        }

        @Test
        @DisplayName("Вернет пустой ответ когда изображение пустое")
        void shouldReturnEmptyResponseWhenImageIsEmpty() {

            when(postService.getPostImage(VALID_POST_ID)).thenReturn(new byte[0]);

            ResponseEntity<byte[]> response = postController.getPostImage(VALID_POST_ID);


            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNull();
            assertThat(response.getHeaders().getContentLength()).isZero();

            verify(postService).getPostImage(VALID_POST_ID);
        }

        @Test
        @DisplayName("Вернет изображение с правильным Content-Length")
        void shouldReturnImageWithCorrectContentLength() {
             byte[] largeImage = new byte[1024];
            when(postService.getPostImage(VALID_POST_ID)).thenReturn(largeImage);

            ResponseEntity<byte[]> response = postController.getPostImage(VALID_POST_ID);

            assertThat(response.getHeaders().getContentLength()).isEqualTo(1024);
        }
    }

}
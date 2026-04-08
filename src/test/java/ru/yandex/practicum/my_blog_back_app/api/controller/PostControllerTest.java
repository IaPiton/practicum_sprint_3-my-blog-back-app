package ru.yandex.practicum.my_blog_back_app.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.PostCreateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.PostUpdateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostListResponse;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostPreview;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostResponse;
import ru.yandex.practicum.my_blog_back_app.api.handler.ApiExceptionHandler;
import ru.yandex.practicum.my_blog_back_app.core.service.PostService;
import ru.yandex.practicum.my_blog_back_app.core.validator.ImageValidator;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты PostController с MockMvc")
class PostControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PostService postService;

    @Mock
    private ImageValidator imageValidator;

    private static final Long VALID_POST_ID = 1L;
    private static final Long INVALID_POST_ID = 999L;

    private PostResponse samplePostResponse;
    private PostCreateRequest sampleCreateRequest;
    private PostUpdateRequest sampleUpdateRequest;
    private PostListResponse samplePostListResponse;

    @BeforeEach
    void setUp() {
        samplePostResponse = PostResponse.builder()
                .id(VALID_POST_ID)
                .title("Заголовок поста")
                .text("Текст")
                .tags(List.of("Tag_1", "Tag_2"))
                .likesCount(10L)
                .build();

        sampleCreateRequest = PostCreateRequest.builder()
                .title("Заголовок поста")
                .text("Текст")
                .tags(List.of("Tag_1", "Tag_2"))
                .build();

        sampleUpdateRequest = PostUpdateRequest.builder()
                .id(VALID_POST_ID)
                .title("Заголовок поста")
                .text("Текст")
                .tags(List.of("Tag_1", "Tag_2"))
                .build();

        PostPreview postPreview = PostPreview.builder()
                .id(VALID_POST_ID)
                .title("Заголовок поста")
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

        PostController postController = new PostController(postService, imageValidator);

        mockMvc = MockMvcBuilders
                .standaloneSetup(postController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("Тесты для GET api/posts")
    class GetPostsTests {

        @Test
        @DisplayName("Возвращает список постов с параметрами по умолчанию")
        void shouldReturnPostsWithDefaultParams() throws Exception {
            when(postService.getPosts("", 0, 5)).thenReturn(samplePostListResponse);

            mockMvc.perform(get("/api/posts")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts", hasSize(1)))
                    .andExpect(jsonPath("$.posts[0].id").value(VALID_POST_ID))
                    .andExpect(jsonPath("$.posts[0].title").value("Заголовок поста"))
                    .andExpect(jsonPath("$.hasPrev").value(true))
                    .andExpect(jsonPath("$.hasNext").value(true))
                    .andExpect(jsonPath("$.lastPage").value(10));

            verify(postService).getPosts("", 0, 5);
        }

        @ParameterizedTest
        @CsvSource({
                "java, 0, 5",
                "spring, 1, 10",
                "test, 2, 20"
        })
        @DisplayName("Возвращает список постов с переданными параметрами")
        void shouldReturnPostsWithCustomParams(String search, int pageNumber, int pageSize) throws Exception {
            when(postService.getPosts(search, pageNumber, pageSize)).thenReturn(samplePostListResponse);

            mockMvc.perform(get("/api/posts")
                            .param("search", search)
                            .param("pageNumber", String.valueOf(pageNumber))
                            .param("pageSize", String.valueOf(pageSize))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(postService).getPosts(search, pageNumber, pageSize);
        }

        @Test
        @DisplayName("Корректирует отрицательный pageNumber на 0")
        void shouldCorrectNegativePageNumber() throws Exception {
            when(postService.getPosts("", 0, 5)).thenReturn(samplePostListResponse);

            mockMvc.perform(get("/api/posts")
                            .param("pageNumber", "-5")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(postService).getPosts("", 0, 5);
        }

        @Test
        @DisplayName("Корректирует pageSize меньше 1 на 5")
        void shouldCorrectInvalidPageSize() throws Exception {
            when(postService.getPosts("", 0, 5)).thenReturn(samplePostListResponse);

            mockMvc.perform(get("/api/posts")
                            .param("pageSize", "0")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(postService).getPosts("", 0, 5);
        }
    }

    @Nested
    @DisplayName("Тесты для GET api/posts/{id}")
    class GetPostByIdTests {

        @Test
        @DisplayName("Возвращает пост по ID")
        void shouldReturnPostWhenExists() throws Exception {
            when(postService.getPostById(VALID_POST_ID)).thenReturn(samplePostResponse);

            mockMvc.perform(get("/api/posts/{id}", VALID_POST_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(VALID_POST_ID))
                    .andExpect(jsonPath("$.title").value("Заголовок поста"))
                    .andExpect(jsonPath("$.text").value("Текст"))
                    .andExpect(jsonPath("$.likesCount").value(10));

            verify(postService).getPostById(VALID_POST_ID);
        }

        @Test
        @DisplayName("Возвращает BAD_REQUEST когда пост не существует")
        void shouldReturnBadRequestWhenPostDoesNotExist() throws Exception {
            when(postService.getPostById(INVALID_POST_ID)).thenThrow(new IllegalArgumentException("Пост не найден"));

            mockMvc.perform(get("/api/posts/{id}", INVALID_POST_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(postService).getPostById(INVALID_POST_ID);
        }
    }

    @Nested
    @DisplayName("Тесты для POST api/posts")
    class CreatePostTests {

        @Test
        @DisplayName("Создает и возвращает пост")
        void shouldCreatePostWhenValidRequest() throws Exception {
            when(postService.createPost(any(PostCreateRequest.class))).thenReturn(samplePostResponse);

            mockMvc.perform(post("/api/posts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(VALID_POST_ID))
                    .andExpect(jsonPath("$.title").value("Заголовок поста"))
                    .andExpect(jsonPath("$.text").value("Текст"));

            verify(postService).createPost(any(PostCreateRequest.class));
        }

        @Test
        @DisplayName("Возвращает 400 при невалидном запросе - пустой заголовок")
        void shouldReturnBadRequestWhenTitleIsEmpty() throws Exception {
            PostCreateRequest invalidRequest = PostCreateRequest.builder()
                    .title("")
                    .text("Содержимое")
                    .build();

            mockMvc.perform(post("/api/posts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(postService, never()).createPost(any());
        }

        @Test
        @DisplayName("Возвращает 400 при невалидном запросе - пустое содержимое")
        void shouldReturnBadRequestWhenContentIsEmpty() throws Exception {
            PostCreateRequest invalidRequest = PostCreateRequest.builder()
                    .title("Заголовок")
                    .text("")
                    .build();

            mockMvc.perform(post("/api/posts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(postService, never()).createPost(any());
        }
    }

    @Nested
    @DisplayName("Тесты для PUT api/posts/{id}")
    class UpdatePostTests {

        @Test
        @DisplayName("Обновляет и возвращает пост")
        void shouldUpdatePostWhenValidRequest() throws Exception {
            PostResponse updatedResponse = PostResponse.builder()
                    .id(VALID_POST_ID)
                    .title("Обновленный заголовок")
                    .text("Обновленное содержимое")
                    .likesCount(0L)
                    .build();

            when(postService.updatePost(any(PostUpdateRequest.class))).thenReturn(updatedResponse);

            mockMvc.perform(put("/api/posts/{id}", VALID_POST_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleUpdateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(VALID_POST_ID))
                    .andExpect(jsonPath("$.title").value("Обновленный заголовок"))
                    .andExpect(jsonPath("$.text").value("Обновленное содержимое"));

            verify(postService).updatePost(any(PostUpdateRequest.class));
        }

        @Test
        @DisplayName("Возвращает 400 при невалидном запросе")
        void shouldReturnBadRequestWhenInvalidRequest() throws Exception {
            PostUpdateRequest invalidRequest = PostUpdateRequest.builder()
                    .id(VALID_POST_ID)
                    .title(null)
                    .text(null)
                    .build();

            mockMvc.perform(put("/api/posts/{id}", VALID_POST_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(postService, never()).updatePost(any());
        }
    }

    @Nested
    @DisplayName("Тесты для DELETE api/posts/{id}")
    class DeletePostTests {

        @Test
        @DisplayName("Удаляет пост")
        void shouldDeletePostWhenExists() throws Exception {
            doNothing().when(postService).deletePost(VALID_POST_ID);

            mockMvc.perform(delete("/api/posts/{id}", VALID_POST_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(postService).deletePost(VALID_POST_ID);
        }
    }

    @Nested
    @DisplayName("Тесты для POST api/posts/{id}/likes")
    class IncrementLikesTests {

        @Test
        @DisplayName("Увеличивает количество лайков и возвращает новое значение")
        void shouldIncrementLikesAndReturnNewCount() throws Exception {
            Long newLikesCount = 5L;
            when(postService.incrementLikes(VALID_POST_ID)).thenReturn(newLikesCount);

            mockMvc.perform(post("/api/posts/{id}/likes", VALID_POST_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(newLikesCount.toString()));

            verify(postService).incrementLikes(VALID_POST_ID);
        }
    }

    @Nested
    @DisplayName("Тесты для PUT api/posts/{id}/image")
    class UpdatePostImageTests {

        @Test
        @DisplayName("Обновляет изображение поста")
        void shouldUpdatePostImageWhenValidImage() throws Exception {
            MockMultipartFile image = new MockMultipartFile(
                    "image",
                    "test-image.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test image content".getBytes()
            );

            doNothing().when(imageValidator).validate(image);
            doNothing().when(postService).updatePostImage(eq(VALID_POST_ID), any(byte[].class));

            mockMvc.perform(multipart("/api/posts/{id}/image", VALID_POST_ID)
                            .file(image)
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            })
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                    .andExpect(status().isOk());

            verify(imageValidator).validate(image);
            verify(postService).updatePostImage(eq(VALID_POST_ID), any(byte[].class));
        }

        @Test
        @DisplayName("Возвращает 400 при невалидном изображении")
        void shouldReturnBadRequestWhenInvalidImage() throws Exception {
            MockMultipartFile invalidImage = new MockMultipartFile(
                    "image",
                    "test.txt",
                    MediaType.TEXT_PLAIN_VALUE,
                    "invalid content".getBytes()
            );

            doThrow(new IllegalArgumentException("Недопустимый формат изображения"))
                    .when(imageValidator).validate(invalidImage);

            mockMvc.perform(multipart("/api/posts/{id}/image", VALID_POST_ID)
                            .file(invalidImage)
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            })
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                    .andExpect(status().isBadRequest());

            verify(imageValidator).validate(invalidImage);
            verify(postService, never()).updatePostImage(any(), any());
        }

        @Test
        @DisplayName("Возвращает 500 при ошибке чтения файла")
        void shouldReturnInternalServerErrorWhenIOException() throws Exception {
            MockMultipartFile image = new MockMultipartFile(
                    "image",
                    "test-image.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test image content".getBytes()
            );

            doNothing().when(imageValidator).validate(image);
            doThrow(new RuntimeException("Ошибка при чтении файла"))
                    .when(postService).updatePostImage(eq(VALID_POST_ID), any(byte[].class));

            mockMvc.perform(multipart("/api/posts/{id}/image", VALID_POST_ID)
                            .file(image)
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            })
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                    .andExpect(status().isInternalServerError());

            verify(imageValidator).validate(image);
            verify(postService).updatePostImage(eq(VALID_POST_ID), any(byte[].class));
        }
    }

    @Nested
    @DisplayName("Тесты для GET api/posts/{id}/image")
    class GetPostImageTests {

        @Test
        @DisplayName("Возвращает изображение поста")
        void shouldReturnPostImageWhenExists() throws Exception {
            byte[] imageBytes = "test image data".getBytes();
            when(postService.getPostImage(VALID_POST_ID)).thenReturn(imageBytes);

            mockMvc.perform(get("/api/posts/{id}/image", VALID_POST_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().bytes(imageBytes))
                    .andExpect(header().string("Content-Length", String.valueOf(imageBytes.length)));

            verify(postService).getPostImage(VALID_POST_ID);
        }

        @Test
        @DisplayName("Возвращает пустой ответ, когда изображение не найдено")
        void shouldReturnEmptyResponseWhenImageNotFound() throws Exception {
            when(postService.getPostImage(VALID_POST_ID)).thenReturn(null);

            mockMvc.perform(get("/api/posts/{id}/image", VALID_POST_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().bytes(new byte[0]))
                    .andExpect(header().string("Content-Length", "0"));

            verify(postService).getPostImage(VALID_POST_ID);
        }

        @Test
        @DisplayName("Возвращает пустой ответ, когда массив изображения пустой")
        void shouldReturnEmptyResponseWhenImageEmpty() throws Exception {
            when(postService.getPostImage(VALID_POST_ID)).thenReturn(new byte[0]);

            mockMvc.perform(get("/api/posts/{id}/image", VALID_POST_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().bytes(new byte[0]))
                    .andExpect(header().string("Content-Length", "0"));

            verify(postService).getPostImage(VALID_POST_ID);
        }

    }
}
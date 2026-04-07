package ru.yandex.practicum.my_blog_back_app.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.CommentCreateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.CommentUpdateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.CommentResponse;
import ru.yandex.practicum.my_blog_back_app.api.handler.ApiExceptionHandler;
import ru.yandex.practicum.my_blog_back_app.core.service.CommentService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты CommentController с MockMvc")
class CommentControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CommentService commentService;

    private static final Long VALID_POST_ID = 1L;
    private static final Long INVALID_POST_ID = 999L;
    private static final Long VALID_COMMENT_ID = 100L;
    private static final String POST_ID_STRING = "1";
    private static final String UNDEFINED_STRING = "undefined";

    private CommentResponse sampleCommentResponse;
    private CommentCreateRequest sampleCreateRequest;
    private CommentUpdateRequest sampleUpdateRequest;

    @BeforeEach
    void setUp() {
        sampleCommentResponse = CommentResponse.builder()
                .id(VALID_COMMENT_ID)
                .postId(VALID_POST_ID)
                .text("Первый комментарий")
                .build();

        sampleCreateRequest = CommentCreateRequest.builder()
                .postId(VALID_POST_ID)
                .text("Первый комментарий")
                .build();

        sampleUpdateRequest = CommentUpdateRequest.builder()
                .id(VALID_COMMENT_ID)
                .postId(VALID_POST_ID)
                .text("Обновленный коментарий")
                .build();

        CommentController commentController = new CommentController(commentService);

        mockMvc = MockMvcBuilders.standaloneSetup(commentController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("Тесты для GET api/posts/{postId}/comments")
    class GetCommentsByPostIdTests {

        @Test
        @DisplayName("Возвращать список комментариев")
        void shouldReturnCommentsWhenPostExists() throws Exception {
            List<CommentResponse> expectedComments = List.of(sampleCommentResponse);
            when(commentService.postExists(VALID_POST_ID)).thenReturn(true);
            when(commentService.getCommentsByPostId(VALID_POST_ID)).thenReturn(expectedComments);

            mockMvc.perform(get("/api/posts/{postId}/comments", POST_ID_STRING)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(VALID_COMMENT_ID))
                    .andExpect(jsonPath("$[0].postId").value(VALID_POST_ID))
                    .andExpect(jsonPath("$[0].text").value("Первый комментарий"));

            verify(commentService).postExists(VALID_POST_ID);
            verify(commentService).getCommentsByPostId(VALID_POST_ID);
        }

        @Test
        @DisplayName("Возвращает пустой список, если postId имеет значение \"undefined\"")
        void shouldReturnEmptyListWhenPostIdIsUndefined() throws Exception {
            mockMvc.perform(get("/api/posts/{postId}/comments", UNDEFINED_STRING)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(commentService, never()).postExists(any());
            verify(commentService, never()).getCommentsByPostId(any());
        }

        @Test
        @DisplayName("Выбрасывает исключение, когда пост не существует")
        void shouldThrowExceptionWhenPostDoesNotExist() throws Exception {
            when(commentService.postExists(INVALID_POST_ID)).thenReturn(false);

            mockMvc.perform(get("/api/posts/{postId}/comments", INVALID_POST_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(commentService).postExists(INVALID_POST_ID);
            verify(commentService, never()).getCommentsByPostId(any());
        }
    }

    @Nested
    @DisplayName("Тесты для GET api/posts/{postId}/comments/{commentId}")
    class GetCommentByIdTests {

        @Test
        @DisplayName("Возвращать комментарий, когда существуют post и comment")
        void shouldReturnCommentWhenPostAndCommentExist() throws Exception {
            when(commentService.postExists(VALID_POST_ID)).thenReturn(true);
            when(commentService.getCommentById(VALID_COMMENT_ID)).thenReturn(sampleCommentResponse);

            mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", VALID_POST_ID, VALID_COMMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(VALID_COMMENT_ID))
                    .andExpect(jsonPath("$.postId").value(VALID_POST_ID))
                    .andExpect(jsonPath("$.text").value("Первый комментарий"));

            verify(commentService).postExists(VALID_POST_ID);
            verify(commentService).getCommentById(VALID_COMMENT_ID);
        }

        @Test
        @DisplayName("Выбрасывает исключение, когда пост не существует")
        void shouldThrowExceptionWhenPostDoesNotExist() throws Exception {
            when(commentService.postExists(INVALID_POST_ID)).thenReturn(false);

            mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", INVALID_POST_ID, VALID_COMMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(commentService).postExists(INVALID_POST_ID);
            verify(commentService, never()).getCommentById(any());
        }
    }

    @Nested
    @DisplayName("Тесты для POST api/posts/{postId}/comments")
    class CreateCommentTests {

        @Test
        @DisplayName("Создает и возвращает комментарий, когда запись существует")
        void shouldCreateCommentWhenPostExists() throws Exception {
            when(commentService.postExists(VALID_POST_ID)).thenReturn(true);
            when(commentService.createComment(any(CommentCreateRequest.class))).thenReturn(sampleCommentResponse);

            mockMvc.perform(post("/api/posts/{postId}/comments", VALID_POST_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(VALID_COMMENT_ID))
                    .andExpect(jsonPath("$.postId").value(VALID_POST_ID))
                    .andExpect(jsonPath("$.text").value("Первый комментарий"));

            verify(commentService).postExists(VALID_POST_ID);
            verify(commentService).createComment(any(CommentCreateRequest.class));
        }

        @Test
        @DisplayName("Выбрасывает исключение, когда пост не существует")
        void shouldThrowExceptionWhenPostDoesNotExist() throws Exception {
            when(commentService.postExists(INVALID_POST_ID)).thenReturn(false);

            mockMvc.perform(post("/api/posts/{postId}/comments", INVALID_POST_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isBadRequest());

            verify(commentService).postExists(INVALID_POST_ID);
            verify(commentService, never()).createComment(any());
        }

        @Test
        @DisplayName("Возвращает 400 при невалидном запросе")
        void shouldReturnBadRequestWhenInvalidRequest() throws Exception {
            CommentCreateRequest invalidRequest = CommentCreateRequest.builder()
                    .postId(null)
                    .text("")
                    .build();

            mockMvc.perform(post("/api/posts/{postId}/comments", VALID_POST_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(commentService, never()).postExists(any());
            verify(commentService, never()).createComment(any());
        }
    }

    @Nested
    @DisplayName("Тесты для PUT api/posts/{postId}/comments/{commentId}")
    class UpdateCommentTests {

        @Test
        @DisplayName("Обновляет и возвращает комментарий, когда запись существует")
        void shouldUpdateCommentWhenPostExists() throws Exception {
            CommentResponse updatedResponse = CommentResponse.builder()
                    .id(VALID_COMMENT_ID)
                    .postId(VALID_POST_ID)
                    .text("Комментарий обновлен")
                    .build();

            when(commentService.postExists(VALID_POST_ID)).thenReturn(true);
            when(commentService.updateComment(eq(VALID_COMMENT_ID), any(CommentUpdateRequest.class)))
                    .thenReturn(updatedResponse);

            mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", VALID_POST_ID, VALID_COMMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleUpdateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(VALID_COMMENT_ID))
                    .andExpect(jsonPath("$.postId").value(VALID_POST_ID))
                    .andExpect(jsonPath("$.text").value("Комментарий обновлен"));

            verify(commentService).postExists(VALID_POST_ID);
            verify(commentService).updateComment(eq(VALID_COMMENT_ID), any(CommentUpdateRequest.class));
        }

        @Test
        @DisplayName("Выбрасывает исключение, когда пост не существует")
        void shouldThrowExceptionWhenPostDoesNotExist() throws Exception {
            CommentUpdateRequest requestWithInvalidPost = CommentUpdateRequest.builder()
                    .postId(INVALID_POST_ID)
                    .text("Первый комментарий")
                    .build();

            when(commentService.postExists(INVALID_POST_ID)).thenReturn(false);

            mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", INVALID_POST_ID, VALID_COMMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithInvalidPost)))
                    .andExpect(status().isBadRequest());

            verify(commentService).postExists(INVALID_POST_ID);
            verify(commentService, never()).updateComment(any(), any());
        }
    }

    @Nested
    @DisplayName("Тесты для DELETE api/posts/{postId}/comments/{commentId}")
    class DeleteCommentTests {

        @Test
        @DisplayName("Удаляет комментарий когда запись существует")
        void shouldDeleteCommentWhenPostExists() throws Exception {
            when(commentService.postExists(VALID_POST_ID)).thenReturn(true);
            doNothing().when(commentService).deleteComment(VALID_COMMENT_ID);

            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", VALID_POST_ID, VALID_COMMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(commentService).postExists(VALID_POST_ID);
            verify(commentService).deleteComment(VALID_COMMENT_ID);
        }

        @Test
        @DisplayName("Выбрасывает исключение, когда пост не существует")
        void shouldThrowExceptionWhenPostDoesNotExist() throws Exception {
            when(commentService.postExists(INVALID_POST_ID)).thenReturn(false);

            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", INVALID_POST_ID, VALID_COMMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(commentService).postExists(INVALID_POST_ID);
            verify(commentService, never()).deleteComment(any());
        }
    }
}
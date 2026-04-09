package ru.yandex.practicum.my_blog_back_app.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.CommentCreateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.CommentUpdateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.CommentResponse;
import ru.yandex.practicum.my_blog_back_app.api.handler.EntityNotFoundException;
import ru.yandex.practicum.my_blog_back_app.core.service.CommentService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@ActiveProfiles("test")
@DisplayName("Тесты CommentController с Spring Boot Test")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    private static final Long VALID_POST_ID = 1L;
    private static final Long INVALID_POST_ID = 999L;
    private static final Long VALID_COMMENT_ID = 100L;
    private static final Long INVALID_COMMENT_ID = 999L;

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
                .text("Обновленный комментарий")
                .build();
    }

    @Nested
    @DisplayName("Тесты для GET /api/posts/{postId}/comments")
    class GetCommentsByPostIdTests {

        @Test
        @DisplayName("Должен вернуть список комментариев когда пост существует")
        void shouldReturnCommentsWhenPostExists() throws Exception {
            List<CommentResponse> expectedComments = List.of(sampleCommentResponse);
            when(commentService.postExists(VALID_POST_ID)).thenReturn(true);
            when(commentService.getCommentsByPostId(VALID_POST_ID)).thenReturn(expectedComments);

            mockMvc.perform(get("/api/posts/{postId}/comments", VALID_POST_ID)
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
        @DisplayName("Должен вернуть пустой список если у поста нет комментариев")
        void shouldReturnEmptyListWhenPostHasNoComments() throws Exception {
            when(commentService.postExists(VALID_POST_ID)).thenReturn(true);
            when(commentService.getCommentsByPostId(VALID_POST_ID)).thenReturn(List.of());

            mockMvc.perform(get("/api/posts/{postId}/comments", VALID_POST_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(commentService).postExists(VALID_POST_ID);
            verify(commentService).getCommentsByPostId(VALID_POST_ID);
        }

        @Test
        @DisplayName("Должен вернуть ошибку когда пост не существует")
        void shouldReturnErrorWhenPostDoesNotExist() throws Exception {
            when(commentService.postExists(INVALID_POST_ID)).thenReturn(false);

            mockMvc.perform(get("/api/posts/{postId}/comments", INVALID_POST_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Пост с id: " + INVALID_POST_ID + " не найден"));

            verify(commentService).postExists(INVALID_POST_ID);
            verify(commentService, never()).getCommentsByPostId(any());
        }

        @Test
        @DisplayName("Должен вернуть ошибку при невалидном postId")
        void shouldReturnErrorWhenPostIdIsInvalid() throws Exception {
            mockMvc.perform(get("/api/posts/{postId}/comments", "invalid")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(commentService, never()).postExists(any());
            verify(commentService, never()).getCommentsByPostId(any());
        }
    }

    @Nested
    @DisplayName("Тесты для GET /api/posts/{postId}/comments/{commentId}")
    class GetCommentByIdTests {

        @Test
        @DisplayName("Должен вернуть комментарий когда пост и комментарий существуют")
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
        @DisplayName("Должен вернуть ошибку когда пост не существует")
        void shouldReturnErrorWhenPostDoesNotExist() throws Exception {
            when(commentService.postExists(INVALID_POST_ID)).thenReturn(false);

            mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", INVALID_POST_ID, VALID_COMMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Пост с id: " + INVALID_POST_ID + " не найден"));

            verify(commentService).postExists(INVALID_POST_ID);
            verify(commentService, never()).getCommentById(any());
        }

        @Test
        @DisplayName("Должен вернуть ошибку когда комментарий не существует")
        void shouldReturnErrorWhenCommentDoesNotExist() throws Exception {
            when(commentService.postExists(VALID_POST_ID)).thenReturn(true);
            when(commentService.getCommentById(INVALID_COMMENT_ID))
                    .thenThrow(new EntityNotFoundException());

            mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", VALID_POST_ID, INVALID_COMMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Запись не найднен"));

            verify(commentService).postExists(VALID_POST_ID);
            verify(commentService).getCommentById(INVALID_COMMENT_ID);
        }
    }

    @Nested
    @DisplayName("Тесты для POST /api/posts/{postId}/comments")
    class CreateCommentTests {

        @Test
        @DisplayName("Должен создать и вернуть комментарий когда пост существует")
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
        @DisplayName("Должен вернуть ошибку когда пост не существует")
        void shouldReturnErrorWhenPostDoesNotExist() throws Exception {
            when(commentService.postExists(INVALID_POST_ID)).thenReturn(false);

            mockMvc.perform(post("/api/posts/{postId}/comments", INVALID_POST_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Пост с id: " + INVALID_POST_ID + " не найден"));

            verify(commentService).postExists(INVALID_POST_ID);
            verify(commentService, never()).createComment(any());
        }

        @Test
        @DisplayName("Должен вернуть ошибку при невалидном запросе (текст пустой)")
        void shouldReturnBadRequestWhenTextIsEmpty() throws Exception {
            CommentCreateRequest invalidRequest = CommentCreateRequest.builder()
                    .postId(VALID_POST_ID)
                    .text("")
                    .build();

            mockMvc.perform(post("/api/posts/{postId}/comments", VALID_POST_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Text пустой"));

            verify(commentService, never()).postExists(any());
            verify(commentService, never()).createComment(any());
        }

    }

    @Nested
    @DisplayName("Тесты для PUT /api/posts/{postId}/comments/{commentId}")
    class UpdateCommentTests {

        @Test
        @DisplayName("Должен обновить и вернуть комментарий когда пост и комментарий существуют")
        void shouldUpdateCommentWhenPostAndCommentExist() throws Exception {
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
        @DisplayName("Должен вернуть ошибку когда пост не существует")
        void shouldReturnErrorWhenPostDoesNotExist() throws Exception {
            when(commentService.postExists(INVALID_POST_ID)).thenReturn(false);

            mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", INVALID_POST_ID, VALID_COMMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleUpdateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Пост с id: " + VALID_POST_ID + " не найден"));

            verify(commentService).postExists(VALID_POST_ID);
            verify(commentService, never()).updateComment(any(), any());
        }

        @Test
        @DisplayName("Должен вернуть ошибку когда комментарий не существует")
        void shouldReturnErrorWhenCommentDoesNotExist() throws Exception {
            when(commentService.postExists(VALID_POST_ID)).thenReturn(true);
            when(commentService.updateComment(eq(INVALID_COMMENT_ID), any(CommentUpdateRequest.class)))
                    .thenThrow(new EntityNotFoundException());

            mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", VALID_POST_ID, INVALID_COMMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleUpdateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Запись не найднен"));

            verify(commentService).postExists(VALID_POST_ID);
            verify(commentService).updateComment(eq(INVALID_COMMENT_ID), any(CommentUpdateRequest.class));
        }

        @Test
        @DisplayName("Должен вернуть ошибку при невалидном запросе (текст пустой)")
        void shouldReturnBadRequestWhenTextIsEmpty() throws Exception {
            CommentUpdateRequest invalidRequest = CommentUpdateRequest.builder()
                    .id(VALID_COMMENT_ID)
                    .postId(VALID_POST_ID)
                    .text("")
                    .build();

            mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", VALID_POST_ID, VALID_COMMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Text пустой"));

            verify(commentService, never()).postExists(any());
            verify(commentService, never()).updateComment(any(), any());
        }
    }

    @Nested
    @DisplayName("Тесты для DELETE /api/posts/{postId}/comments/{commentId}")
    class DeleteCommentTests {

        @Test
        @DisplayName("Должен удалить комментарий когда пост существует")
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
        @DisplayName("Должен вернуть ошибку когда пост не существует")
        void shouldReturnErrorWhenPostDoesNotExist() throws Exception {
            when(commentService.postExists(INVALID_POST_ID)).thenReturn(false);

            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", INVALID_POST_ID, VALID_COMMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Пост с id: " + INVALID_POST_ID + " не найден"));

            verify(commentService).postExists(INVALID_POST_ID);
            verify(commentService, never()).deleteComment(any());
        }

        @Test
        @DisplayName("Должен успешно выполнить удаление даже если комментарий не существует")
        void shouldSucceedEvenWhenCommentDoesNotExist() throws Exception {
            when(commentService.postExists(VALID_POST_ID)).thenReturn(true);
            doNothing().when(commentService).deleteComment(INVALID_COMMENT_ID);

            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", VALID_POST_ID, INVALID_COMMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(commentService).postExists(VALID_POST_ID);
            verify(commentService).deleteComment(INVALID_COMMENT_ID);
        }
    }
}
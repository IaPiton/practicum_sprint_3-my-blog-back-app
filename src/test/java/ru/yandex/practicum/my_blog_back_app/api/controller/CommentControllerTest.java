package ru.yandex.practicum.my_blog_back_app.api.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.CommentCreateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.CommentUpdateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.CommentResponse;
import ru.yandex.practicum.my_blog_back_app.core.service.CommentService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты CommentController")
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private static final Long VALID_POST_ID = 1L;
    private static final Long INVALID_POST_ID = 999L;
    private static final Long VALID_COMMENT_ID = 100L;
    private static final String POST_ID_STRING = "1";
    private static final String UNDEFINED_STRING = "undefined";

    private static CommentResponse sampleCommentResponse;
    private static CommentCreateRequest sampleCreateRequest;
    private static CommentUpdateRequest sampleUpdateRequest;

    @BeforeAll
    static void setUp() {
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
    }

    @Nested
    @DisplayName("Тесты для getCommentsByPostId()")
    class GetCommentsByPostIdTests {

        @Test
        @DisplayName("Возвращать список комментариев")
        void shouldReturnCommentsWhenPostExists() {
            List<CommentResponse> expectedComments = List.of(sampleCommentResponse);
            when(commentService.postExists(VALID_POST_ID)).thenReturn(true);
            when(commentService.getCommentsByPostId(VALID_POST_ID)).thenReturn(expectedComments);

            ResponseEntity<List<CommentResponse>> response = commentController.getCommentsByPostId(POST_ID_STRING);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().getFirst()).isEqualTo(sampleCommentResponse);

            verify(commentService).postExists(VALID_POST_ID);
            verify(commentService).getCommentsByPostId(VALID_POST_ID);
        }

        @Test
        @DisplayName("Возвращает пустой список, если postId имеет значение \"undefined\"")
        void shouldReturnEmptyListWhenPostIdIsUndefined() {
            ResponseEntity<List<CommentResponse>> response = commentController.getCommentsByPostId(UNDEFINED_STRING);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();

            verify(commentService, never()).postExists(any());
            verify(commentService, never()).getCommentsByPostId(any());
        }

        @Test
        @DisplayName("Исключение IllegalArgumentException, когда post не существует")
        void shouldThrowExceptionWhenPostDoesNotExist() {
            when(commentService.postExists(INVALID_POST_ID)).thenReturn(false);

             assertThatThrownBy(() -> commentController.getCommentsByPostId(String.valueOf(INVALID_POST_ID)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(String.format("Пост с id: %d не найден", INVALID_POST_ID));

            verify(commentService).postExists(INVALID_POST_ID);
            verify(commentService, never()).getCommentsByPostId(any());
        }
    }

    @Nested
    @DisplayName("Тесты для getCommentById()")
    class GetCommentByIdTests {

        @Test
        @DisplayName("Возвращать комментарий, когда существуют post и comment")
        void shouldReturnCommentWhenPostAndCommentExist() {
            when(commentService.postExists(VALID_POST_ID)).thenReturn(true);
            when(commentService.getCommentById(VALID_COMMENT_ID)).thenReturn(sampleCommentResponse);

            ResponseEntity<CommentResponse> response = commentController.getCommentById(VALID_POST_ID, VALID_COMMENT_ID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEqualTo(sampleCommentResponse);

            verify(commentService).postExists(VALID_POST_ID);
            verify(commentService).getCommentById(VALID_COMMENT_ID);
        }

        @Test
        @DisplayName("Исключение IllegalArgumentException, когда post не существует")
        void shouldThrowExceptionWhenPostDoesNotExist() {
            when(commentService.postExists(INVALID_POST_ID)).thenReturn(false);

            assertThatThrownBy(() -> commentController.getCommentById(INVALID_POST_ID, VALID_COMMENT_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(String.format("Пост с id: %d не найден", INVALID_POST_ID));

            verify(commentService).postExists(INVALID_POST_ID);
            verify(commentService, never()).getCommentById(any());
        }
    }

    @Nested
    @DisplayName("Тесты для createComment()")
    class CreateCommentTests {

        @Test
        @DisplayName("Создает и возвращать комментарий, когда запись существует")
        void shouldCreateCommentWhenPostExists() {
            when(commentService.postExists(VALID_POST_ID)).thenReturn(true);
            when(commentService.createComment(sampleCreateRequest)).thenReturn(sampleCommentResponse);

            ResponseEntity<CommentResponse> response = commentController.createComment(VALID_POST_ID, sampleCreateRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEqualTo(sampleCommentResponse);

            verify(commentService).postExists(VALID_POST_ID);
            verify(commentService).createComment(sampleCreateRequest);
        }

        @Test
        @DisplayName("Исключение IllegalArgumentException, когда post не существует")
        void shouldThrowExceptionWhenPostDoesNotExist() {
            when(commentService.postExists(INVALID_POST_ID)).thenReturn(false);

            assertThatThrownBy(() -> commentController.createComment(INVALID_POST_ID, sampleCreateRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(String.format("Пост с id: %d не найден", INVALID_POST_ID));

            verify(commentService).postExists(INVALID_POST_ID);
            verify(commentService, never()).createComment(any());
        }
    }

    @Nested
    @DisplayName("Тесты для updateComment()")
    class UpdateCommentTests {

        @Test
        @DisplayName("Обновляет и возвращает комментарий, когда запись существует")
        void shouldUpdateCommentWhenPostExists() {
            CommentResponse updatedResponse = CommentResponse.builder()
                    .id(VALID_COMMENT_ID)
                    .postId(VALID_POST_ID)
                    .text("Комментарий обновлен")
                    .build();

            when(commentService.postExists(VALID_POST_ID)).thenReturn(true);
            when(commentService.updateComment(VALID_COMMENT_ID, sampleUpdateRequest)).thenReturn(updatedResponse);

            ResponseEntity<CommentResponse> response = commentController.updateComment(VALID_COMMENT_ID, sampleUpdateRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getText()).isEqualTo("Комментарий обновлен");

            verify(commentService).postExists(VALID_POST_ID);
            verify(commentService).updateComment(VALID_COMMENT_ID, sampleUpdateRequest);
        }

        @Test
        @DisplayName("Исключение IllegalArgumentException, когда post не существует")
        void shouldThrowExceptionWhenPostDoesNotExist() {
            CommentUpdateRequest requestWithInvalidPost = CommentUpdateRequest.builder()
                    .postId(INVALID_POST_ID)
                    .text("Первый комментаррий")
                    .build();

            when(commentService.postExists(INVALID_POST_ID)).thenReturn(false);

            assertThatThrownBy(() -> commentController.updateComment(VALID_COMMENT_ID, requestWithInvalidPost))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(String.format("Пост с id: %d не найден", INVALID_POST_ID));

            verify(commentService).postExists(INVALID_POST_ID);
            verify(commentService, never()).updateComment(any(), any());
        }
    }

    @Nested
    @DisplayName("Тесты для deleteComment()")
    class DeleteCommentTests {

        @Test
        @DisplayName("Удаляет комментарий когда запись существует")
        void shouldDeleteCommentWhenPostExists() {
            when(commentService.postExists(VALID_POST_ID)).thenReturn(true);
            doNothing().when(commentService).deleteComment(VALID_COMMENT_ID);

            ResponseEntity<Void> response = commentController.deleteComment(VALID_POST_ID, VALID_COMMENT_ID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNull();

            verify(commentService).postExists(VALID_POST_ID);
            verify(commentService).deleteComment(VALID_COMMENT_ID);
        }

        @Test
        @DisplayName("Исключение IllegalArgumentException, когда post не существует")
        void shouldThrowExceptionWhenPostDoesNotExist() {
             when(commentService.postExists(INVALID_POST_ID)).thenReturn(false);

            assertThatThrownBy(() -> commentController.deleteComment(INVALID_POST_ID, VALID_COMMENT_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(String.format("Пост с id: %d не найден", INVALID_POST_ID));

            verify(commentService).postExists(INVALID_POST_ID);
            verify(commentService, never()).deleteComment(any());
        }
    }
}
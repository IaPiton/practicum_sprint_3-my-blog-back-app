package ru.yandex.practicum.my_blog_back_app.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.CommentCreateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.CommentUpdateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.CommentResponse;
import ru.yandex.practicum.my_blog_back_app.api.handler.EntityNotFoundException;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.CommentsEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.mapper.CommentMapper;
import ru.yandex.practicum.my_blog_back_app.persistence.repository.CommentRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса комментариев")
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    private CommentsEntity testCommentEntity;
    private CommentResponse testCommentResponse;
    private CommentCreateRequest testCreateRequest;
    private CommentUpdateRequest testUpdateRequest;

    @BeforeEach
    void setUp() {
        testCommentEntity = new CommentsEntity();
        testCommentEntity.setId(1L);
        testCommentEntity.setPostId(100L);
        testCommentEntity.setText("Тестовый комментарий");

        testCommentResponse = CommentResponse.builder()
                .id(1L)
                .postId(100L)
                .text("Тестовый комментарий")
                .build();

        testCreateRequest = CommentCreateRequest.builder()
                .postId(100L)
                .text("Новый комментарий")
                .build();

        testUpdateRequest = CommentUpdateRequest.builder()
                .text("Обновленный комментарий")
                .build();
    }

    @Nested
    @DisplayName("Тесты получения комментариев")
    class GetCommentsTests {

        @Test
        @DisplayName("Должен вернуть список комментариев по Id поста")
        void shouldReturnCommentsListByPostId() {
            List<CommentsEntity> commentsEntities = List.of(testCommentEntity);
            when(commentRepository.findCommentsByPostId(100L)).thenReturn(commentsEntities);
            when(commentMapper.toResponse(testCommentEntity)).thenReturn(testCommentResponse);

            List<CommentResponse> result = commentService.getCommentsByPostId(100L);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(testCommentResponse);
            verify(commentRepository).findCommentsByPostId(100L);
            verify(commentMapper).toResponse(testCommentEntity);
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если у поста нет комментариев")
        void shouldReturnEmptyListWhenNoComments() {
            when(commentRepository.findCommentsByPostId(200L)).thenReturn(List.of());

            List<CommentResponse> result = commentService.getCommentsByPostId(200L);

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            verify(commentRepository).findCommentsByPostId(200L);
            verify(commentMapper, never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("Тесты получения комментария по Id")
    class GetCommentByIdTests {

        @Test
        @DisplayName("Должен вернуть комментарий по существующему Id")
        void shouldReturnCommentById() {
            when(commentRepository.findById(1L)).thenReturn(Optional.of(testCommentEntity));
            when(commentMapper.toResponse(testCommentEntity)).thenReturn(testCommentResponse);

            CommentResponse result = commentService.getCommentById(1L);

            assertThat(result).isEqualTo(testCommentResponse);
            verify(commentRepository).findById(1L);
            verify(commentMapper).toResponse(testCommentEntity);
        }

        @Test
        @DisplayName("Должен выбросить исключение, если комментарий не найден")
        void shouldThrowExceptionWhenCommentNotFound() {
            when(commentRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.getCommentById(999L))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(commentRepository).findById(999L);
            verify(commentMapper, never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("Тесты создания комментария")
    class CreateCommentTests {

        @Test
        @DisplayName("Должен успешно создать новый комментарий")
        void shouldCreateCommentSuccessfully() {
            Long generatedId = 5L;

            when(commentRepository.save(any(CommentsEntity.class))).thenReturn(generatedId);
            when(commentMapper.toResponse(any(CommentsEntity.class))).thenReturn(
                    CommentResponse.builder()
                            .id(generatedId)
                            .postId(testCreateRequest.getPostId())
                            .text(testCreateRequest.getText())
                            .build()
            );

            CommentResponse result = commentService.createComment(testCreateRequest);

            assertThat(result).isNotNull();
            assertThat(result.getPostId()).isEqualTo(100L);
            assertThat(result.getText()).isEqualTo("Новый комментарий");
            assertThat(result.getId()).isEqualTo(generatedId);

            verify(commentRepository).save(any(CommentsEntity.class));
            verify(commentMapper).toResponse(any(CommentsEntity.class));
        }

        @Test
        @DisplayName("Должен создать комментарий с корректными полями")
        void shouldCreateCommentWithCorrectFields() {
            when(commentRepository.save(any(CommentsEntity.class))).thenReturn(1L);
            when(commentMapper.toResponse(any(CommentsEntity.class))).thenReturn(testCommentResponse);

            commentService.createComment(testCreateRequest);

            verify(commentRepository).save(argThat(entity ->
                    entity.getPostId().equals(100L) &&
                            "Новый комментарий".equals(entity.getText())
            ));
        }

        @Test
        @DisplayName("Должен установить правильный ID после сохранения")
        void shouldSetCorrectIdAfterSave() {
            Long expectedId = 10L;
            when(commentRepository.save(any(CommentsEntity.class))).thenReturn(expectedId);
            when(commentMapper.toResponse(any(CommentsEntity.class))).thenAnswer(invocation -> {
                CommentsEntity entity = invocation.getArgument(0);
                return CommentResponse.builder()
                        .id(entity.getId())
                        .postId(entity.getPostId())
                        .text(entity.getText())
                        .build();
            });

            CommentResponse result = commentService.createComment(testCreateRequest);

            assertThat(result.getId()).isEqualTo(expectedId);
            verify(commentRepository, times(1)).save(any(CommentsEntity.class));
        }

        @Nested
        @DisplayName("Тесты обновления комментария")
        class UpdateCommentTests {

            @Test
            @DisplayName("Должен успешно обновить существующий комментарий")
            void shouldUpdateCommentSuccessfully() {
                when(commentRepository.findById(1L)).thenReturn(Optional.of(testCommentEntity));
                when(commentMapper.toResponse(testCommentEntity)).thenReturn(
                        CommentResponse.builder()
                                .id(1L)
                                .postId(100L)
                                .text("Обновленный комментарий")
                                .build()
                );

                CommentResponse result = commentService.updateComment(1L, testUpdateRequest);

                assertThat(result).isNotNull();
                assertThat(result.getText()).isEqualTo("Обновленный комментарий");
                assertThat(testCommentEntity.getText()).isEqualTo("Обновленный комментарий");

                verify(commentRepository).findById(1L);
                verify(commentRepository).update(testCommentEntity);
                verify(commentMapper).toResponse(testCommentEntity);
            }

            @Test
            @DisplayName("Должен выбросить исключение при обновлении несуществующего комментария")
            void shouldThrowExceptionWhenUpdatingNonExistentComment() {
                when(commentRepository.findById(999L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> commentService.updateComment(999L, testUpdateRequest))
                        .isInstanceOf(EntityNotFoundException.class);

                verify(commentRepository).findById(999L);
                verify(commentRepository, never()).update(any());
            }

            @Test
            @DisplayName("Должен обновить только текст комментария, не затрагивая другие поля")
            void shouldUpdateOnlyTextField() {
                CommentsEntity originalComment = new CommentsEntity();
                originalComment.setId(1L);
                originalComment.setPostId(100L);
                originalComment.setText("Старый текст");

                when(commentRepository.findById(1L)).thenReturn(Optional.of(originalComment));
                when(commentMapper.toResponse(any(CommentsEntity.class))).thenReturn(
                        CommentResponse.builder()
                                .id(1L)
                                .postId(100L)
                                .text("Новый текст")
                                .build()
                );

                commentService.updateComment(1L, testUpdateRequest);

                assertThat(originalComment.getText()).isEqualTo("Обновленный комментарий");
                assertThat(originalComment.getPostId()).isEqualTo(100L);
                assertThat(originalComment.getId()).isEqualTo(1L);
            }
        }

        @Nested
        @DisplayName("Тесты удаления комментария")
        class DeleteCommentTests {

            @Test
            @DisplayName("Должен успешно удалить комментарий по Id")
            void shouldDeleteCommentById() {
                Long commentId = 1L;
                doNothing().when(commentRepository).deleteByCommentId(commentId);

                commentService.deleteComment(commentId);

                verify(commentRepository).deleteByCommentId(commentId);
            }

            @Test
            @DisplayName("При удалении несуществующего комментария не должно быть ошибки")
            void shouldNotThrowErrorWhenDeletingNonExistentComment() {
                Long nonExistentId = 999L;
                doNothing().when(commentRepository).deleteByCommentId(nonExistentId);

                commentService.deleteComment(nonExistentId);

                verify(commentRepository).deleteByCommentId(nonExistentId);
            }

            @Test
            @DisplayName("Должен корректно удалить несколько комментариев подряд")
            void shouldDeleteMultipleCommentsInRow() {
                Long commentId1 = 1L;
                Long commentId2 = 2L;
                Long commentId3 = 3L;

                commentService.deleteComment(commentId1);
                commentService.deleteComment(commentId2);
                commentService.deleteComment(commentId3);

                verify(commentRepository).deleteByCommentId(commentId1);
                verify(commentRepository).deleteByCommentId(commentId2);
                verify(commentRepository).deleteByCommentId(commentId3);
            }
        }

        @Nested
        @DisplayName("Тесты проверки существования поста")
        class PostExistsTests {

            @Test
            @DisplayName("Должен вернуть true, если пост существует")
            void shouldReturnTrueWhenPostExists() {
                when(commentRepository.postExists(100L)).thenReturn(true);

                boolean result = commentService.postExists(100L);

                assertThat(result).isTrue();
                verify(commentRepository).postExists(100L);
            }

            @Test
            @DisplayName("Должен вернуть false, если пост не существует")
            void shouldReturnFalseWhenPostNotExists() {
                when(commentRepository.postExists(999L)).thenReturn(false);

                boolean result = commentService.postExists(999L);

                assertThat(result).isFalse();
                verify(commentRepository).postExists(999L);
            }

            @Test
            @DisplayName("Должен корректно обработать false при удалении несуществующего поста")
            void shouldHandleFalseWhenPostNotExists() {
                when(commentRepository.postExists(999L)).thenReturn(false);

                boolean result = commentService.postExists(999L);

                assertThat(result).isFalse();
                verify(commentRepository).postExists(999L);
            }
        }

        @Nested
        @DisplayName("Интеграционные тесты потока данных")
        class IntegrationFlowTests {

            @Test
            @DisplayName("Должен корректно обработать полный цикл CRUD операций")
            void shouldHandleFullCrudCycle() {
                when(commentRepository.save(any(CommentsEntity.class))).thenReturn(1L);
                when(commentMapper.toResponse(any(CommentsEntity.class))).thenReturn(testCommentResponse);

                CommentResponse created = commentService.createComment(testCreateRequest);
                assertThat(created).isNotNull();

                when(commentRepository.findById(1L)).thenReturn(Optional.of(testCommentEntity));
                when(commentMapper.toResponse(testCommentEntity)).thenReturn(testCommentResponse);

                CommentResponse found = commentService.getCommentById(1L);
                assertThat(found).isEqualTo(testCommentResponse);

                when(commentRepository.findById(1L)).thenReturn(Optional.of(testCommentEntity));
                CommentResponse updated = commentService.updateComment(1L, testUpdateRequest);
                assertThat(updated).isNotNull();

                commentService.deleteComment(1L);
                verify(commentRepository).deleteByCommentId(1L);
            }
        }
    }
}

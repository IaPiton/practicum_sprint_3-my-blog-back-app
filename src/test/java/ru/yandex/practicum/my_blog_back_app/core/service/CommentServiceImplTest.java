package ru.yandex.practicum.my_blog_back_app.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.CommentCreateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.CommentUpdateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.CommentResponse;
import ru.yandex.practicum.my_blog_back_app.api.handler.EntityNotFoundException;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.CommentEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.PostEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.repository.CommentRepository;
import ru.yandex.practicum.my_blog_back_app.persistence.repository.PostRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Интеграционные тесты сервиса комментариев с реальной БД")
class CommentServiceImplTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    private PostEntity testPost;
    private CommentEntity testCommentEntity;
    private CommentCreateRequest testCreateRequest;
    private CommentUpdateRequest testUpdateRequest;

    @BeforeEach
    void setUp() {
        testPost = new PostEntity();
        testPost.setTitle("Тестовый пост");
        testPost.setText("Содержимое тестового поста");
        testPost.setLikesCount(0L);
        testPost = postRepository.save(testPost);

        testCommentEntity = new CommentEntity();
        testCommentEntity.setPost(testPost);
        testCommentEntity.setText("Тестовый комментарий");
        testCommentEntity = commentRepository.save(testCommentEntity);

        testCreateRequest = CommentCreateRequest.builder()
                .postId(testPost.getId())
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
            List<CommentResponse> result = commentService.getCommentsByPostId(testPost.getId());

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getText()).isEqualTo("Тестовый комментарий");
            assertThat(result.getFirst().getPostId()).isEqualTo(testPost.getId());
        }

        @Test
        @DisplayName("Должен вернуть пустой список, если у поста нет комментариев")
        void shouldReturnEmptyListWhenNoComments() {
            PostEntity newPost = new PostEntity();
            newPost.setTitle("Пост без комментариев");
            newPost.setText("Содержимое");
            newPost.setLikesCount(0L);
            newPost = postRepository.save(newPost);

            List<CommentResponse> result = commentService.getCommentsByPostId(newPost.getId());

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Должен вернуть пустой список для несуществующего поста")
        void shouldReturnEmptyListForNonExistentPost() {
            List<CommentResponse> result = commentService.getCommentsByPostId(99999L);

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Тесты получения комментария по Id")
    class GetCommentByIdTests {

        @Test
        @DisplayName("Должен вернуть комментарий по существующему Id")
        void shouldReturnCommentById() {
            CommentResponse result = commentService.getCommentById(testCommentEntity.getId());

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testCommentEntity.getId());
            assertThat(result.getText()).isEqualTo("Тестовый комментарий");
            assertThat(result.getPostId()).isEqualTo(testPost.getId());
        }

        @Test
        @DisplayName("Должен выбросить исключение, если комментарий не найден")
        void shouldThrowExceptionWhenCommentNotFound() {
            assertThatThrownBy(() -> commentService.getCommentById(99999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Тесты создания комментария")
    class CreateCommentTests {

        @Test
        @DisplayName("Должен успешно создать новый комментарий")
        void shouldCreateCommentSuccessfully() {
            CommentResponse result = commentService.createComment(testCreateRequest);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getPostId()).isEqualTo(testPost.getId());
            assertThat(result.getText()).isEqualTo("Новый комментарий");

            CommentEntity savedComment = commentRepository.findById(result.getId()).orElse(null);
            assertThat(savedComment).isNotNull();
            assertThat(savedComment.getText()).isEqualTo("Новый комментарий");
            assertThat(savedComment.getPost().getId()).isEqualTo(testPost.getId());
        }

        @Test
        @DisplayName("Должен выбросить исключение при создании комментария к несуществующему посту")
        void shouldThrowExceptionWhenPostNotFound() {
            CommentCreateRequest invalidRequest = CommentCreateRequest.builder()
                    .postId(99999L)
                    .text("Комментарий к несуществующему посту")
                    .build();

            assertThatThrownBy(() -> commentService.createComment(invalidRequest))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("Должен создать комментарий с пустым текстом")
        void shouldCreateCommentWithEmptyText() {
            CommentCreateRequest emptyTextRequest = CommentCreateRequest.builder()
                    .postId(testPost.getId())
                    .text("")
                    .build();

            CommentResponse result = commentService.createComment(emptyTextRequest);

            assertThat(result).isNotNull();
            assertThat(result.getText()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Тесты обновления комментария")
    class UpdateCommentTests {

        @Test
        @DisplayName("Должен успешно обновить существующий комментарий")
        void shouldUpdateCommentSuccessfully() {
            CommentResponse result = commentService.updateComment(testCommentEntity.getId(), testUpdateRequest);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testCommentEntity.getId());
            assertThat(result.getText()).isEqualTo("Обновленный комментарий");

            CommentEntity updatedComment = commentRepository.findById(testCommentEntity.getId()).orElse(null);
            assertThat(updatedComment).isNotNull();
            assertThat(updatedComment.getText()).isEqualTo("Обновленный комментарий");
        }

        @Test
        @DisplayName("Должен выбросить исключение при обновлении несуществующего комментария")
        void shouldThrowExceptionWhenUpdatingNonExistentComment() {
            assertThatThrownBy(() -> commentService.updateComment(99999L, testUpdateRequest))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("Должен обновить комментарий на пустой текст")
        void shouldUpdateCommentToEmptyText() {
            CommentUpdateRequest emptyTextRequest = CommentUpdateRequest.builder()
                    .text("")
                    .build();

            CommentResponse result = commentService.updateComment(testCommentEntity.getId(), emptyTextRequest);

            assertThat(result).isNotNull();
            assertThat(result.getText()).isEmpty();

            CommentEntity updatedComment = commentRepository.findById(testCommentEntity.getId()).orElse(null);
            assertThat(updatedComment).isNotNull();
            assertThat(updatedComment.getText()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Тесты удаления комментария")
    class DeleteCommentTests {

        @Test
        @DisplayName("Должен успешно удалить комментарий по Id")
        void shouldDeleteCommentById() {
            commentService.deleteComment(testCommentEntity.getId());

            boolean exists = commentRepository.existsById(testCommentEntity.getId());
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("При удалении несуществующего комментария не должно быть ошибки")
        void shouldNotThrowErrorWhenDeletingNonExistentComment() {
            commentService.deleteComment(99999L);
        }

        @Test
        @DisplayName("Должен корректно удалить несколько комментариев подряд")
        void shouldDeleteMultipleCommentsInRow() {
            CommentEntity comment2 = new CommentEntity();
            comment2.setPost(testPost);
            comment2.setText("Второй комментарий");
            comment2 = commentRepository.save(comment2);

            CommentEntity comment3 = new CommentEntity();
            comment3.setPost(testPost);
            comment3.setText("Третий комментарий");
            comment3 = commentRepository.save(comment3);

            commentService.deleteComment(testCommentEntity.getId());
            commentService.deleteComment(comment2.getId());
            commentService.deleteComment(comment3.getId());

            assertThat(commentRepository.existsById(testCommentEntity.getId())).isFalse();
            assertThat(commentRepository.existsById(comment2.getId())).isFalse();
            assertThat(commentRepository.existsById(comment3.getId())).isFalse();
        }
    }

    @Nested
    @DisplayName("Тесты проверки существования комментариев у поста")
    class PostExistsTests {

        @Test
        @DisplayName("Должен вернуть true, если у поста есть комментарии")
        void shouldReturnTrueWhenPostHasComments() {
            boolean result = commentService.postExists(testPost.getId());

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Должен вернуть false для несуществующего поста")
        void shouldReturnFalseForNonExistentPost() {
            boolean result = commentService.postExists(99999L);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Интеграционные тесты потока данных")
    class IntegrationFlowTests {

        @Test
        @DisplayName("Должен корректно обработать полный цикл CRUD операций")
        void shouldHandleFullCrudCycle() {
            CommentResponse created = commentService.createComment(testCreateRequest);
            assertThat(created).isNotNull();
            Long commentId = created.getId();

            CommentResponse found = commentService.getCommentById(commentId);
            assertThat(found.getText()).isEqualTo("Новый комментарий");
            assertThat(found.getPostId()).isEqualTo(testPost.getId());

            CommentUpdateRequest updateRequest = CommentUpdateRequest.builder()
                    .text("Обновленный текст")
                    .build();
            CommentResponse updated = commentService.updateComment(commentId, updateRequest);
            assertThat(updated.getText()).isEqualTo("Обновленный текст");

            CommentResponse verified = commentService.getCommentById(commentId);
            assertThat(verified.getText()).isEqualTo("Обновленный текст");

            commentService.deleteComment(commentId);

            assertThatThrownBy(() -> commentService.getCommentById(commentId))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("Должен корректно обработать несколько комментариев к одному посту")
        void shouldHandleMultipleCommentsForSamePost() {
            CommentCreateRequest request2 = CommentCreateRequest.builder()
                    .postId(testPost.getId())
                    .text("Второй комментарий")
                    .build();

            CommentCreateRequest request3 = CommentCreateRequest.builder()
                    .postId(testPost.getId())
                    .text("Третий комментарий")
                    .build();

            commentService.createComment(request2);
            commentService.createComment(request3);

            List<CommentResponse> comments = commentService.getCommentsByPostId(testPost.getId());

            assertThat(comments).hasSize(3);
            assertThat(comments).extracting(CommentResponse::getText)
                    .containsExactlyInAnyOrder("Тестовый комментарий", "Второй комментарий", "Третий комментарий");
        }

        @Test
        @DisplayName("Должен правильно обработать комментарии к разным постам")
        void shouldHandleCommentsForDifferentPosts() {
            PostEntity secondPost = new PostEntity();
            secondPost.setTitle("Второй пост");
            secondPost.setText("Содержимое второго поста");
            secondPost.setLikesCount(0L);
            secondPost = postRepository.save(secondPost);

            CommentCreateRequest requestForSecondPost = CommentCreateRequest.builder()
                    .postId(secondPost.getId())
                    .text("Комментарий ко второму посту")
                    .build();
            commentService.createComment(requestForSecondPost);

            List<CommentResponse> firstPostComments = commentService.getCommentsByPostId(testPost.getId());
            assertThat(firstPostComments).hasSize(1);
            assertThat(firstPostComments.getFirst().getText()).isEqualTo("Тестовый комментарий");

            List<CommentResponse> secondPostComments = commentService.getCommentsByPostId(secondPost.getId());
            assertThat(secondPostComments).hasSize(1);
            assertThat(secondPostComments.getFirst().getText()).isEqualTo("Комментарий ко второму посту");
        }
    }
}
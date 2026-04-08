package ru.yandex.practicum.my_blog_back_app.persistence.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yandex.practicum.my_blog_back_app.configuration.TestCommonConfiguration;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.CommentsEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@DisplayName("Тесты репозитория комментариев")
class CommentRepositoryImplTest extends TestCommonConfiguration {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long testPostId;
    private CommentsEntity testComment;

    @BeforeEach
    void setUp() {
        transactionTemplate.execute(status -> {
            jdbcClient.sql("DELETE FROM blog.comments").update();
            jdbcClient.sql("DELETE FROM blog.post_tags").update();
            jdbcClient.sql("DELETE FROM blog.posts").update();
            jdbcClient.sql("DELETE FROM blog.tags").update();
            return null;
        });

        transactionTemplate.execute(status -> {
            LocalDateTime now = LocalDateTime.now();

            testPostId = jdbcClient.sql("""
                    INSERT INTO blog.posts (title, text, likes_count, image, create_at, update_at)
                    VALUES (:title, :text, :likesCount, :image, :createAt, :updateAt)
                    RETURNING id
                    """)
                    .param("title", "Тестовый пост")
                    .param("text", "Содержание тестового поста")
                    .param("likesCount", 0L)
                    .param("image", null)
                    .param("createAt", now)
                    .param("updateAt", now)
                    .query(Long.class)
                    .single();
            return null;
        });

        testComment = new CommentsEntity();
        testComment.setText("Тестовый комментарий");
        testComment.setPostId(testPostId);
    }

    @Test
    @DisplayName("Должен сохранить комментарий")
    void shouldSaveComment() {
        Long commentId = commentRepository.save(testComment);

        assertThat(commentId).isNotNull();
        assertThat(commentId).isPositive();

        Optional<CommentsEntity> savedComment = commentRepository.findById(commentId);
        assertThat(savedComment).isPresent();
        assertThat(savedComment.get().getText()).isEqualTo("Тестовый комментарий");
        assertThat(savedComment.get().getPostId()).isEqualTo(testPostId);
        assertThat(savedComment.get().getCreateAt()).isNotNull();
        assertThat(savedComment.get().getUpdateAt()).isNotNull();
    }

    @Test
    @DisplayName("Должен найти комментарий по ID")
    void shouldFindCommentById() {
        Long commentId = commentRepository.save(testComment);

        Optional<CommentsEntity> foundComment = commentRepository.findById(commentId);

        assertThat(foundComment).isPresent();
        assertThat(foundComment.get().getId()).isEqualTo(commentId);
        assertThat(foundComment.get().getText()).isEqualTo("Тестовый комментарий");
        assertThat(foundComment.get().getPostId()).isEqualTo(testPostId);
    }

    @Test
    @DisplayName("Должен вернуть Optional.empty() при поиске несуществующего комментария")
    void shouldReturnEmptyOptionalWhenCommentNotFound() {
        Optional<CommentsEntity> foundComment = commentRepository.findById(999L);

        assertThat(foundComment).isEmpty();
    }

    @Test
    @DisplayName("Должен найти комментарии по ID поста")
    void shouldFindCommentsByPostId() {
        commentRepository.save(testComment);

        CommentsEntity secondComment = new CommentsEntity();
        secondComment.setText("Второй комментарий");
        secondComment.setPostId(testPostId);
        commentRepository.save(secondComment);

        List<CommentsEntity> comments = commentRepository.findCommentsByPostId(testPostId);

        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getText()).isEqualTo("Тестовый комментарий");
        assertThat(comments.get(1).getText()).isEqualTo("Второй комментарий");
    }

    @Test
    @DisplayName("Должен вернуть пустой список если у поста нет комментариев")
    void shouldReturnEmptyListWhenNoCommentsForPost() {
        List<CommentsEntity> comments = commentRepository.findCommentsByPostId(999L);

        assertThat(comments).isEmpty();
    }

    @Test
    @DisplayName("Должен обновить комментарий")
    void shouldUpdateComment() {
        Long commentId = commentRepository.save(testComment);
        Optional<CommentsEntity> savedCommentOpt = commentRepository.findById(commentId);
        assertThat(savedCommentOpt).isPresent();

        CommentsEntity savedComment = savedCommentOpt.get();
        savedComment.setText("Обновленный комментарий");

        commentRepository.update(savedComment);

        Optional<CommentsEntity> updatedCommentOpt = commentRepository.findById(commentId);
        assertThat(updatedCommentOpt).isPresent();

        CommentsEntity updatedComment = updatedCommentOpt.get();
        assertThat(updatedComment.getText()).isEqualTo("Обновленный комментарий");
        assertThat(updatedComment.getUpdateAt()).isAfter(savedComment.getUpdateAt());
        assertThat(updatedComment.getPostId()).isEqualTo(testPostId);
    }

    @Test
    @DisplayName("Должен удалить комментарий по ID")
    void shouldDeleteCommentById() {
        Long commentId = commentRepository.save(testComment);

        Optional<CommentsEntity> beforeDelete = commentRepository.findById(commentId);
        assertThat(beforeDelete).isPresent();

        commentRepository.deleteByCommentId(commentId);

        Optional<CommentsEntity> afterDelete = commentRepository.findById(commentId);
        assertThat(afterDelete).isEmpty();
    }

    @Test
    @DisplayName("Должен удалить все комментарии по ID поста")
    void shouldDeleteCommentsByPostId() {
        commentRepository.save(testComment);

        CommentsEntity secondComment = new CommentsEntity();
        secondComment.setText("Второй комментарий");
        secondComment.setPostId(testPostId);
        commentRepository.save(secondComment);

        List<CommentsEntity> beforeDelete = commentRepository.findCommentsByPostId(testPostId);
        assertThat(beforeDelete).hasSize(2);

        commentRepository.deleteByPostId(testPostId);

        List<CommentsEntity> afterDelete = commentRepository.findCommentsByPostId(testPostId);
        assertThat(afterDelete).isEmpty();
    }

    @Test
    @DisplayName("Должен подсчитать количество комментариев у поста")
    void shouldCountCommentsByPost() {
        commentRepository.save(testComment);
        commentRepository.save(testComment);
        commentRepository.save(testComment);

        Long count = commentRepository.countCommentsByPost(testPostId);

        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Должен вернуть 0 при подсчете комментариев если у поста нет комментариев")
    void shouldReturnZeroWhenNoCommentsForPost() {
        Long count = commentRepository.countCommentsByPost(999L);

        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Должен вернуть true если пост существует")
    void shouldReturnTrueWhenPostExists() {
        boolean exists = commentRepository.postExists(testPostId);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Должен вернуть false если пост не существует")
    void shouldReturnFalseWhenPostNotExists() {
        boolean exists = commentRepository.postExists(999L);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Должен вернуть комментарии в порядке создания (по возрастанию)")
    void shouldReturnCommentsInCreationOrder() {
        CommentsEntity firstComment = new CommentsEntity();
        firstComment.setText("Первый комментарий");
        firstComment.setPostId(testPostId);
        Long firstId = commentRepository.save(firstComment);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        CommentsEntity secondComment = new CommentsEntity();
        secondComment.setText("Второй комментарий");
        secondComment.setPostId(testPostId);
        Long secondId = commentRepository.save(secondComment);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        CommentsEntity thirdComment = new CommentsEntity();
        thirdComment.setText("Третий комментарий");
        thirdComment.setPostId(testPostId);
        Long thirdId = commentRepository.save(thirdComment);

        List<CommentsEntity> comments = commentRepository.findCommentsByPostId(testPostId);

        assertThat(comments).hasSize(3);
        assertThat(comments.get(0).getId()).isEqualTo(firstId);
        assertThat(comments.get(1).getId()).isEqualTo(secondId);
        assertThat(comments.get(2).getId()).isEqualTo(thirdId);
    }

    @Test
    @DisplayName("Должен корректно обработать комментарий с длинным текстом")
    void shouldHandleLongTextComment() {
        String longText = "Очень длинный текст комментария, который содержит множество символов. ".repeat(100);
        testComment.setText(longText);

        Long commentId = commentRepository.save(testComment);

        Optional<CommentsEntity> savedComment = commentRepository.findById(commentId);
        assertThat(savedComment).isPresent();
        assertThat(savedComment.get().getText()).isEqualTo(longText);
    }

    @Test
    @DisplayName("Должен корректно обработать несколько комментариев от разных постов")
    void shouldHandleCommentsFromDifferentPosts() {
        Long secondPostId = transactionTemplate.execute(status -> {
            LocalDateTime now = LocalDateTime.now();

            return jdbcClient.sql("""
                    INSERT INTO blog.posts (title, text, likes_count, image, create_at, update_at)
                    VALUES (:title, :text, :likesCount, :image, :createAt, :updateAt)
                    RETURNING id
                    """)
                    .param("title", "Второй пост")
                    .param("text", "Содержание второго поста")
                    .param("likesCount", 0L)
                    .param("image", null)
                    .param("createAt", now)
                    .param("updateAt", now)
                    .query(Long.class)
                    .single();
        });

        commentRepository.save(testComment);

        CommentsEntity commentForSecondPost = new CommentsEntity();
        commentForSecondPost.setText("Комментарий для второго поста");
        commentForSecondPost.setPostId(secondPostId);
        commentRepository.save(commentForSecondPost);

        List<CommentsEntity> firstPostComments = commentRepository.findCommentsByPostId(testPostId);
        List<CommentsEntity> secondPostComments = commentRepository.findCommentsByPostId(secondPostId);

        assertThat(firstPostComments).hasSize(1);
        assertThat(secondPostComments).hasSize(1);
        assertThat(firstPostComments.getFirst().getPostId()).isEqualTo(testPostId);
        assertThat(secondPostComments.getFirst().getPostId()).isEqualTo(secondPostId);
    }

    @Test
    @DisplayName("Должен корректно обработать удаление несуществующего комментария")
    void shouldHandleDeleteNonExistentComment() {
        commentRepository.deleteByCommentId(999L);

        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("Должен корректно обработать удаление комментариев несуществующего поста")
    void shouldHandleDeleteCommentsByNonExistentPost() {
        commentRepository.deleteByPostId(999L);

        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("Должен обновить комментарий несколько раз подряд")
    void shouldUpdateCommentMultipleTimes() {
        Long commentId = commentRepository.save(testComment);
        Optional<CommentsEntity> commentOpt = commentRepository.findById(commentId);
        assertThat(commentOpt).isPresent();

        CommentsEntity comment = commentOpt.get();

        comment.setText("Обновление 1");
        commentRepository.update(comment);

        comment.setText("Обновление 2");
        commentRepository.update(comment);

        comment.setText("Обновление 3");
        commentRepository.update(comment);

        Optional<CommentsEntity> finalCommentOpt = commentRepository.findById(commentId);
        assertThat(finalCommentOpt).isPresent();
        assertThat(finalCommentOpt.get().getText()).isEqualTo("Обновление 3");
    }

    @Test
    @DisplayName("Должен выбросить исключение при сохранении комментария с null текстом")
    void shouldThrowExceptionWhenSaveCommentWithNullText() {
        testComment.setText(null);

        assertThatThrownBy(() -> commentRepository.save(testComment))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
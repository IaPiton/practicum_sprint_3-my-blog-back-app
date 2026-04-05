package ru.yandex.practicum.my_blog_back_app.persistence.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.CommentsEntity;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringJUnitConfig
@Testcontainers
@DisplayName("Тесты репозитория комментариев")
class CommentRepositoryImplTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.5")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Configuration
    static class TestConfig {

        @Bean
        public DataSource dataSourceTest() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(postgres.getDriverClassName());
            dataSource.setUrl(postgres.getJdbcUrl());
            dataSource.setUsername(postgres.getUsername());
            dataSource.setPassword(postgres.getPassword());
            return dataSource;
        }

        @Bean
        public NamedParameterJdbcTemplate namedParameterJdbcTemplateTest(DataSource dataSource) {
            return new NamedParameterJdbcTemplate(dataSource);
        }

        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
            return new TransactionTemplate(transactionManager);
        }

        @Bean
        public CommentRepository commentRepository(NamedParameterJdbcTemplate jdbcTemplate) {
            return new CommentRepositoryImpl(jdbcTemplate);
        }
    }

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long testPostId;
    private CommentsEntity testComment;

    @BeforeEach
    void setUp() {
        transactionTemplate.execute(status -> {
            jdbcTemplate.getJdbcTemplate().execute("DELETE FROM blog.comments");
            jdbcTemplate.getJdbcTemplate().execute("DELETE FROM blog.post_tags");
            jdbcTemplate.getJdbcTemplate().execute("DELETE FROM blog.posts");
            jdbcTemplate.getJdbcTemplate().execute("DELETE FROM blog.tags");
            return null;
        });

        transactionTemplate.execute(status -> {
            String insertPostSql = """
                    INSERT INTO blog.posts (title, text, likes_count, image, create_at, update_at)
                    VALUES (:title, :text, :likesCount, :image, :createAt, :updateAt)
                    RETURNING id
                    """;

            LocalDateTime now = LocalDateTime.now();
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("title", "Тестовый пост");
            params.addValue("text", "Содержание тестового поста");
            params.addValue("likesCount", 0L);
            params.addValue("image", null);
            params.addValue("createAt", now);
            params.addValue("updateAt", now);

            testPostId = jdbcTemplate.queryForObject(insertPostSql, params, Long.class);
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

        CommentsEntity savedComment = commentRepository.findById(commentId);
        assertThat(savedComment).isNotNull();
        assertThat(savedComment.getText()).isEqualTo("Тестовый комментарий");
        assertThat(savedComment.getPostId()).isEqualTo(testPostId);
        assertThat(savedComment.getCreateAt()).isNotNull();
        assertThat(savedComment.getUpdateAt()).isNotNull();
    }

    @Test
    @DisplayName("Должен найти комментарий по ID")
    void shouldFindCommentById() {
        Long commentId = commentRepository.save(testComment);

        CommentsEntity foundComment = commentRepository.findById(commentId);

        assertThat(foundComment).isNotNull();
        assertThat(foundComment.getId()).isEqualTo(commentId);
        assertThat(foundComment.getText()).isEqualTo("Тестовый комментарий");
        assertThat(foundComment.getPostId()).isEqualTo(testPostId);
    }

    @Test
    @DisplayName("Должен выбросить исключение при поиске несуществующего комментария")
    void shouldThrowExceptionWhenCommentNotFound() {
        assertThatThrownBy(() -> commentRepository.findById(999L))
                .isInstanceOf(EmptyResultDataAccessException.class);
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
        CommentsEntity savedComment = commentRepository.findById(commentId);
        savedComment.setText("Обновленный комментарий");

        commentRepository.update(savedComment);

        CommentsEntity updatedComment = commentRepository.findById(commentId);
        assertThat(updatedComment.getText()).isEqualTo("Обновленный комментарий");
        assertThat(updatedComment.getUpdateAt()).isAfter(savedComment.getUpdateAt());
        assertThat(updatedComment.getPostId()).isEqualTo(testPostId);
    }

    @Test
    @DisplayName("Должен удалить комментарий по ID")
    void shouldDeleteCommentById() {
        Long commentId = commentRepository.save(testComment);

        CommentsEntity beforeDelete = commentRepository.findById(commentId);
        assertThat(beforeDelete).isNotNull();

        commentRepository.deleteByCommentId(commentId);

        assertThatThrownBy(() -> commentRepository.findById(commentId))
                .isInstanceOf(EmptyResultDataAccessException.class);
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

        CommentsEntity savedComment = commentRepository.findById(commentId);
        assertThat(savedComment.getText()).isEqualTo(longText);
    }

    @Test
    @DisplayName("Должен корректно обработать несколько комментариев от разных постов")
    void shouldHandleCommentsFromDifferentPosts() {
        Long secondPostId = transactionTemplate.execute(status -> {
            String insertPostSql = """
                    INSERT INTO blog.posts (title, text, likes_count, image, create_at, update_at)
                    VALUES (:title, :text, :likesCount, :image, :createAt, :updateAt)
                    RETURNING id
                    """;

            LocalDateTime now = LocalDateTime.now();
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("title", "Второй пост");
            params.addValue("text", "Содержание второго поста");
            params.addValue("likesCount", 0L);
            params.addValue("image", null);
            params.addValue("createAt", now);
            params.addValue("updateAt", now);

            return jdbcTemplate.queryForObject(insertPostSql, params, Long.class);
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
        CommentsEntity comment = commentRepository.findById(commentId);

        comment.setText("Обновление 1");
        commentRepository.update(comment);

        comment.setText("Обновление 2");
        commentRepository.update(comment);

        comment.setText("Обновление 3");
        commentRepository.update(comment);

        CommentsEntity finalComment = commentRepository.findById(commentId);
        assertThat(finalComment.getText()).isEqualTo("Обновление 3");
    }

    @Test
    @DisplayName("Должен корректно обработать сохранение комментария с null текстом")
    void shouldHandleSaveCommentWithNullText() {
        testComment.setText(null);

        assertThatThrownBy(() -> commentRepository.save(testComment))
                .isInstanceOf(Exception.class);
    }
}
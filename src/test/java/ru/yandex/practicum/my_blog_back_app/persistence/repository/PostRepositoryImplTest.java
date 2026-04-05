package ru.yandex.practicum.my_blog_back_app.persistence.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import ru.yandex.practicum.my_blog_back_app.persistence.entity.PostEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.TagEntity;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@Testcontainers
@DisplayName("Тесты репозитория постов")
class PostRepositoryImplTest {

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
        public TagRepository tagRepository(NamedParameterJdbcTemplate jdbcTemplate) {
            return new TagRepositoryImpl(jdbcTemplate);
        }

        @Bean
        public PostRepository postRepository(NamedParameterJdbcTemplate jdbcTemplate, TagRepository tagRepository) {
            return new PostRepositoryImpl(jdbcTemplate, tagRepository);
        }
    }

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private PostEntity testPost;
    private TagEntity testTag1;
    private TagEntity testTag2;

    @BeforeEach
    void setUp() {
        // Очистка данных перед каждым тестом
        transactionTemplate.execute(status -> {
            jdbcTemplate.getJdbcTemplate().execute("DELETE FROM blog.post_tags");
            jdbcTemplate.getJdbcTemplate().execute("DELETE FROM blog.posts");
            jdbcTemplate.getJdbcTemplate().execute("DELETE FROM blog.tags");
            return null;
        });

        // Сохраняем теги напрямую в БД
        transactionTemplate.execute(status -> {
            String insertTagSql = "INSERT INTO blog.tags (name) VALUES (:name) RETURNING id";

            MapSqlParameterSource params1 = new MapSqlParameterSource();
            params1.addValue("name", "java");
            Long tagId1 = jdbcTemplate.queryForObject(insertTagSql, params1, Long.class);

            MapSqlParameterSource params2 = new MapSqlParameterSource();
            params2.addValue("name", "spring");
            Long tagId2 = jdbcTemplate.queryForObject(insertTagSql, params2, Long.class);

            testTag1 = new TagEntity();
            testTag1.setId(tagId1);
            testTag1.setName("java");

            testTag2 = new TagEntity();
            testTag2.setId(tagId2);
            testTag2.setName("spring");

            return null;
        });

        // Подготовка тестового поста
        testPost = new PostEntity();
        testPost.setTitle("Тестовый пост");
        testPost.setText("Содержание тестового поста");
        testPost.setLikesCount(0L);
        testPost.setImage(null);
        testPost.setTags(List.of(testTag1, testTag2));
    }

    @Test
    @DisplayName("Должен сохранить пост без тегов")
    void shouldSavePostWithoutTags() {
        PostEntity postWithoutTags = new PostEntity();
        postWithoutTags.setTitle("Пост без тегов");
        postWithoutTags.setText("Содержание");
        postWithoutTags.setLikesCount(0L);
        postWithoutTags.setTags(null);

        PostEntity savedPost = postRepository.savePost(postWithoutTags);

        assertThat(savedPost.getId()).isNotNull();
        assertThat(savedPost.getTitle()).isEqualTo("Пост без тегов");
        assertThat(savedPost.getTags()).isNull();

        String countSql = "SELECT COUNT(*) FROM blog.post_tags WHERE post_id = :postId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", savedPost.getId());
        Integer count = jdbcTemplate.queryForObject(countSql, params, Integer.class);
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Должен сохранить пост с тегами")
    void shouldSavePostWithTags() {
        PostEntity savedPost = postRepository.savePost(testPost);

        assertThat(savedPost.getId()).isNotNull();
        assertThat(savedPost.getTitle()).isEqualTo("Тестовый пост");
        assertThat(savedPost.getTags()).hasSize(2);

        String countSql = "SELECT COUNT(*) FROM blog.post_tags WHERE post_id = :postId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", savedPost.getId());
        Integer count = jdbcTemplate.queryForObject(countSql, params, Integer.class);
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Должен найти пост по ID")
    void shouldFindPostById() {
        PostEntity savedPost = postRepository.savePost(testPost);

        Optional<PostEntity> foundPost = postRepository.findById(savedPost.getId());

        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getId()).isEqualTo(savedPost.getId());
        assertThat(foundPost.get().getTitle()).isEqualTo("Тестовый пост");
        assertThat(foundPost.get().getTags()).hasSize(2);
    }

    @Test
    @DisplayName("Должен вернуть пустой Optional если пост не найден")
    void shouldReturnEmptyOptionalWhenPostNotFound() {
        Optional<PostEntity> foundPost = postRepository.findById(999L);

        assertThat(foundPost).isEmpty();
    }

    @Test
    @DisplayName("Должен найти посты с фильтрацией по заголовку")
    void shouldFindPostsWithTitleFilter() {
        postRepository.savePost(testPost);

        PostEntity anotherPost = new PostEntity();
        anotherPost.setTitle("Другой пост");
        anotherPost.setText("Другое содержание");
        anotherPost.setLikesCount(0L);
        postRepository.savePost(anotherPost);

        List<PostEntity> foundPosts = postRepository.findPostsWithFilters(
                "тестовый", null, 10, 0
        );

        assertThat(foundPosts).hasSize(1);
        assertThat(foundPosts.getFirst().getTitle()).isEqualTo("Тестовый пост");
    }

    @Test
    @DisplayName("Должен найти посты с фильтрацией по тегам")
    void shouldFindPostsWithTagFilter() {
        postRepository.savePost(testPost);

        PostEntity anotherPost = new PostEntity();
        anotherPost.setTitle("Пост без тегов");
        anotherPost.setText("Содержание");
        anotherPost.setLikesCount(0L);
        postRepository.savePost(anotherPost);

        List<PostEntity> foundPosts = postRepository.findPostsWithFilters(
                null, List.of("java"), 10, 0
        );

        assertThat(foundPosts).hasSize(1);
        assertThat(foundPosts.getFirst().getTitle()).isEqualTo("Тестовый пост");
    }

    @Test
    @DisplayName("Должен обновить пост")
    void shouldUpdatePost() {
        PostEntity savedPost = postRepository.savePost(testPost);
        savedPost.setTitle("Обновленный заголовок");
        savedPost.setLikesCount(10L);

        postRepository.update(savedPost);

        Optional<PostEntity> updatedPost = postRepository.findById(savedPost.getId());
        assertThat(updatedPost).isPresent();
        assertThat(updatedPost.get().getTitle()).isEqualTo("Обновленный заголовок");
        assertThat(updatedPost.get().getLikesCount()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Должен удалить пост")
    void shouldDeletePost() {
        PostEntity savedPost = postRepository.savePost(testPost);

        Optional<PostEntity> beforeDelete = postRepository.findById(savedPost.getId());
        assertThat(beforeDelete).isPresent();

        postRepository.delete(savedPost.getId());

        Optional<PostEntity> afterDelete = postRepository.findById(savedPost.getId());
        assertThat(afterDelete).isEmpty();
    }

    @Test
    @DisplayName("Должен вернуть пустой список если посты не найдены")
    void shouldReturnEmptyListWhenNoPostsFound() {
        List<PostEntity> foundPosts = postRepository.findPostsWithFilters(
                "несуществующий", null, 10, 0
        );

        assertThat(foundPosts).isEmpty();
    }

    @Test
    @DisplayName("Должен подсчитать все посты без фильтров")
    void shouldCountAllPostsWithoutFilters() {
        postRepository.savePost(testPost);

        PostEntity anotherPost = new PostEntity();
        anotherPost.setTitle("Другой пост");
        anotherPost.setText("Другое содержание");
        anotherPost.setLikesCount(0L);
        postRepository.savePost(anotherPost);

        PostEntity thirdPost = new PostEntity();
        thirdPost.setTitle("Третий пост");
        thirdPost.setText("Третье содержание");
        thirdPost.setLikesCount(0L);
        postRepository.savePost(thirdPost);

        int count = postRepository.countPostsWithFilters(null, null);

        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Должен подсчитать посты с фильтрацией по заголовку (частичное совпадение)")
    void shouldCountPostsWithTitleFilterPartialMatch() {
        postRepository.savePost(testPost);

        PostEntity anotherPost = new PostEntity();
        anotherPost.setTitle("Другой пост");
        anotherPost.setText("Другое содержание");
        anotherPost.setLikesCount(0L);
        postRepository.savePost(anotherPost);

        PostEntity similarPost = new PostEntity();
        similarPost.setTitle("Тестовый пост номер 2");
        similarPost.setText("Содержание");
        similarPost.setLikesCount(0L);
        postRepository.savePost(similarPost);

        int count = postRepository.countPostsWithFilters("тестовый", null);

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Должен подсчитать посты с фильтрацией по одному тегу")
    void shouldCountPostsWithSingleTagFilter() {
        postRepository.savePost(testPost);

        PostEntity postWithOnlyJava = new PostEntity();
        postWithOnlyJava.setTitle("Только Java");
        postWithOnlyJava.setText("Содержание");
        postWithOnlyJava.setLikesCount(0L);
        postWithOnlyJava.setTags(List.of(testTag1));
        postRepository.savePost(postWithOnlyJava);

        PostEntity postWithOnlySpring = new PostEntity();
        postWithOnlySpring.setTitle("Только Spring");
        postWithOnlySpring.setText("Содержание");
        postWithOnlySpring.setLikesCount(0L);
        postWithOnlySpring.setTags(List.of(testTag2));
        postRepository.savePost(postWithOnlySpring);

        int count = postRepository.countPostsWithFilters(null, List.of("java"));

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Должен подсчитать посты с фильтрацией по нескольким тегам (AND логика)")
    void shouldCountPostsWithMultipleTagsAndLogic() {
        postRepository.savePost(testPost);

        PostEntity postWithOnlyJava = new PostEntity();
        postWithOnlyJava.setTitle("Только Java");
        postWithOnlyJava.setText("Содержание");
        postWithOnlyJava.setLikesCount(0L);
        postWithOnlyJava.setTags(List.of(testTag1));
        postRepository.savePost(postWithOnlyJava);

        PostEntity postWithOnlySpring = new PostEntity();
        postWithOnlySpring.setTitle("Только Spring");
        postWithOnlySpring.setText("Содержание");
        postWithOnlySpring.setLikesCount(0L);
        postWithOnlySpring.setTags(List.of(testTag2));
        postRepository.savePost(postWithOnlySpring);

        int count = postRepository.countPostsWithFilters(null, List.of("java", "spring"));

        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Должен подсчитать посты с фильтрацией по заголовку и тегам одновременно")
    void shouldCountPostsWithTitleAndTagFilters() {
        postRepository.savePost(testPost);

        PostEntity anotherPost = new PostEntity();
        anotherPost.setTitle("Тестовый пост без тегов");
        anotherPost.setText("Содержание");
        anotherPost.setLikesCount(0L);
        postRepository.savePost(anotherPost);

        PostEntity postWithDifferentTitle = new PostEntity();
        postWithDifferentTitle.setTitle("Другой пост");
        postWithDifferentTitle.setText("Содержание");
        postWithDifferentTitle.setLikesCount(0L);
        postWithDifferentTitle.setTags(List.of(testTag1));
        postRepository.savePost(postWithDifferentTitle);

        int count = postRepository.countPostsWithFilters("тестовый", List.of("java"));

        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Должен вернуть 0 при подсчете если посты не найдены")
    void shouldReturnZeroWhenNoPostsMatchFilters() {
        postRepository.savePost(testPost);

        int countWithNonExistentTitle = postRepository.countPostsWithFilters("несуществующий", null);
        int countWithNonExistentTag = postRepository.countPostsWithFilters(null, List.of("python"));
        int countWithBothFilters = postRepository.countPostsWithFilters("несуществующий", List.of("python"));

        assertThat(countWithNonExistentTitle).isZero();
        assertThat(countWithNonExistentTag).isZero();
        assertThat(countWithBothFilters).isZero();
    }

    @Test
    @DisplayName("Должен корректно подсчитать посты с пустыми параметрами фильтров")
    void shouldCountPostsWithEmptyFilterParameters() {
        postRepository.savePost(testPost);

        PostEntity anotherPost = new PostEntity();
        anotherPost.setTitle("Другой пост");
        anotherPost.setText("Содержание");
        anotherPost.setLikesCount(0L);
        postRepository.savePost(anotherPost);

        int countWithEmptyTitle = postRepository.countPostsWithFilters("", null);
        int countWithEmptyTags = postRepository.countPostsWithFilters(null, List.of());
        int countWithBothEmpty = postRepository.countPostsWithFilters("", List.of());

        assertThat(countWithEmptyTitle).isEqualTo(2);
        assertThat(countWithEmptyTags).isEqualTo(2);
        assertThat(countWithBothEmpty).isEqualTo(2);
    }

    @Test
    @DisplayName("Должен игнорировать регистр при подсчете по заголовку")
    void shouldIgnoreCaseWhenCountingByTitle() {
        postRepository.savePost(testPost);

        int countLower = postRepository.countPostsWithFilters("тестовый", null);
        int countUpper = postRepository.countPostsWithFilters("ТЕСТОВЫЙ", null);
        int countMixed = postRepository.countPostsWithFilters("ТеСтОвЫй", null);

        assertThat(countLower).isEqualTo(1);
        assertThat(countUpper).isEqualTo(1);
        assertThat(countMixed).isEqualTo(1);
    }

    @Test
    @DisplayName("Должен игнорировать регистр при подсчете по тегам")
    void shouldIgnoreCaseWhenCountingByTags() {
        postRepository.savePost(testPost);

        int countLower = postRepository.countPostsWithFilters(null, List.of("java"));
        int countUpper = postRepository.countPostsWithFilters(null, List.of("JAVA"));
        int countMixed = postRepository.countPostsWithFilters(null, List.of("JaVa"));

        assertThat(countLower).isEqualTo(1);
        assertThat(countUpper).isEqualTo(1);
        assertThat(countMixed).isEqualTo(1);
    }

    @Test
    @DisplayName("Должен корректно подсчитать посты с тегом, который есть у нескольких постов")
    void shouldCountPostsWithTagThatAppearsInMultiplePosts() {
        postRepository.savePost(testPost);

        PostEntity anotherPostWithJava = new PostEntity();
        anotherPostWithJava.setTitle("Еще один пост с Java");
        anotherPostWithJava.setText("Содержание");
        anotherPostWithJava.setLikesCount(0L);
        anotherPostWithJava.setTags(List.of(testTag1));
        postRepository.savePost(anotherPostWithJava);

        PostEntity postWithoutJava = new PostEntity();
        postWithoutJava.setTitle("Пост без Java");
        postWithoutJava.setText("Содержание");
        postWithoutJava.setLikesCount(0L);
        postWithoutJava.setTags(List.of(testTag2));
        postRepository.savePost(postWithoutJava);

        int count = postRepository.countPostsWithFilters(null, List.of("java"));

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Должен корректно подсчитать посты с null значениями в БД")
    void shouldCountPostsWithNullValuesInDatabase() {
        PostEntity postWithNullImage = new PostEntity();
        postWithNullImage.setTitle("Пост с null изображением");
        postWithNullImage.setText("Содержание");
        postWithNullImage.setLikesCount(0L);
        postWithNullImage.setImage(null);
        postRepository.savePost(postWithNullImage);

        int count = postRepository.countPostsWithFilters(null, null);

        assertThat(count).isEqualTo(1);
    }
}
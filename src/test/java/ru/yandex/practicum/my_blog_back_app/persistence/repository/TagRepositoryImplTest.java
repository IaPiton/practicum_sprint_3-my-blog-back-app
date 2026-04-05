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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@Testcontainers
@DisplayName("Тесты репозитория тегов")
class TagRepositoryImplTest {

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
    }

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long testPostId;
    private PostEntity testPost;

    @BeforeEach
    void setUp() {
        transactionTemplate.execute(status -> {
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

            testPost = new PostEntity();
            testPost.setId(testPostId);
            testPost.setTitle("Тестовый пост");
            testPost.setText("Содержание тестового поста");
            return null;
        });
    }

    @Test
    @DisplayName("Должен получить существующие теги и создать новые")
    void shouldGetExistingAndCreateNewTags() {
        String insertTagSql = "INSERT INTO blog.tags (name) VALUES (:name) RETURNING id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", "java");
        Long existingTagId = jdbcTemplate.queryForObject(insertTagSql, params, Long.class);

        List<String> tagNames = List.of("java", "spring", "kotlin");
        List<TagEntity> tags = tagRepository.getTags(tagNames);

        assertThat(tags).hasSize(3);

        TagEntity javaTag = tags.stream()
                .filter(t -> t.getName().equals("java"))
                .findFirst()
                .orElse(null);
        assertThat(javaTag).isNotNull();
        assertThat(javaTag.getId()).isEqualTo(existingTagId);

        TagEntity springTag = tags.stream()
                .filter(t -> t.getName().equals("spring"))
                .findFirst()
                .orElse(null);
        assertThat(springTag).isNotNull();
        assertThat(springTag.getId()).isNotNull();

        TagEntity kotlinTag = tags.stream()
                .filter(t -> t.getName().equals("kotlin"))
                .findFirst()
                .orElse(null);
        assertThat(kotlinTag).isNotNull();
        assertThat(kotlinTag.getId()).isNotNull();
    }

    @Test
    @DisplayName("Должен создать все теги если их нет в БД")
    void shouldCreateAllTagsWhenNoneExist() {
        List<String> tagNames = List.of("java", "spring", "python");
        List<TagEntity> tags = tagRepository.getTags(tagNames);

        assertThat(tags).hasSize(3);
        tags.forEach(tag -> {
            assertThat(tag.getId()).isNotNull();
            assertThat(tag.getName()).isIn("java", "spring", "python");
        });
    }

    @Test
    @DisplayName("Должен обработать пустой список тегов")
    void shouldHandleEmptyTagsList() {
        List<TagEntity> tags = tagRepository.getTags(List.of());

        assertThat(tags).isEmpty();
    }

    @Test
    @DisplayName("Должен обрезать пробелы в названиях тегов")
    void shouldTrimTagNames() {
        List<String> tagNames = List.of("  java  ", " spring ", "kotlin");
        List<TagEntity> tags = tagRepository.getTags(tagNames);

        assertThat(tags).hasSize(3);
        assertThat(tags.get(0).getName()).isEqualTo("java");
        assertThat(tags.get(1).getName()).isEqualTo("spring");
        assertThat(tags.get(2).getName()).isEqualTo("kotlin");
    }

    @Test
    @DisplayName("Должен игнорировать дубликаты тегов")
    void shouldHandleDuplicateTags() {
        List<String> tagNames = List.of("java", "java", "spring", "java");
        List<TagEntity> tags = tagRepository.getTags(tagNames);

        assertThat(tags).hasSize(4);

        Long javaId = tags.stream()
                .filter(t -> t.getName().equals("java"))
                .findFirst()
                .get()
                .getId();

        boolean allJavaTagsHaveSameId = tags.stream()
                .filter(t -> t.getName().equals("java"))
                .allMatch(t -> t.getId().equals(javaId));
        assertThat(allJavaTagsHaveSameId).isTrue();
    }

    @Test
    @DisplayName("Должен сохранить связи поста с тегами")
    void shouldSaveTagsAndPost() {
        List<String> tagNames = List.of("java", "spring");
        List<TagEntity> tags = tagRepository.getTags(tagNames);
        testPost.setTags(tags);

        tagRepository.saveTagsAndPost(testPost);

        String countSql = "SELECT COUNT(*) FROM blog.post_tags WHERE post_id = :postId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", testPostId);
        Integer count = jdbcTemplate.queryForObject(countSql, params, Integer.class);
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Не должен создавать дублирующиеся связи при повторном сохранении")
    void shouldNotCreateDuplicateRelations() {
        List<String> tagNames = List.of("java", "spring");
        List<TagEntity> tags = tagRepository.getTags(tagNames);
        testPost.setTags(tags);

        tagRepository.saveTagsAndPost(testPost);

        tagRepository.saveTagsAndPost(testPost);

        String countSql = "SELECT COUNT(*) FROM blog.post_tags WHERE post_id = :postId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", testPostId);
        Integer count = jdbcTemplate.queryForObject(countSql, params, Integer.class);
        assertThat(count).isEqualTo(2); // Должно быть 2, а не 4
    }

    @Test
    @DisplayName("Должен найти теги по ID поста")
    void shouldFindTagsByPostId() {
        List<String> tagNames = List.of("java", "spring", "kotlin");
        List<TagEntity> tags = tagRepository.getTags(tagNames);
        testPost.setTags(tags);
        tagRepository.saveTagsAndPost(testPost);

        List<TagEntity> foundTags = tagRepository.findTagsByPostId(testPostId);

        assertThat(foundTags).hasSize(3);
        assertThat(foundTags).extracting("name")
                .containsExactlyInAnyOrder("java", "spring", "kotlin");
    }

    @Test
    @DisplayName("Должен вернуть пустой список если у поста нет тегов")
    void shouldReturnEmptyListWhenPostHasNoTags() {
        List<TagEntity> foundTags = tagRepository.findTagsByPostId(999L);

        assertThat(foundTags).isEmpty();
    }

    @Test
    @DisplayName("Должен удалить все связи поста с тегами")
    void shouldDeleteTagAndPost() {
        List<String> tagNames = List.of("java", "spring");
        List<TagEntity> tags = tagRepository.getTags(tagNames);
        testPost.setTags(tags);
        tagRepository.saveTagsAndPost(testPost);

        String countSql = "SELECT COUNT(*) FROM blog.post_tags WHERE post_id = :postId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", testPostId);
        Integer beforeCount = jdbcTemplate.queryForObject(countSql, params, Integer.class);
        assertThat(beforeCount).isEqualTo(2);

        tagRepository.deleteTagAndPost(testPostId);

        Integer afterCount = jdbcTemplate.queryForObject(countSql, params, Integer.class);
        assertThat(afterCount).isZero();

        String tagsCountSql = "SELECT COUNT(*) FROM blog.tags";
        Integer tagsCount = jdbcTemplate.queryForObject(tagsCountSql, new MapSqlParameterSource(), Integer.class);
        assertThat(tagsCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Должен корректно обработать удаление связей для поста без тегов")
    void shouldHandleDeleteForPostWithoutTags() {
        tagRepository.deleteTagAndPost(testPostId);

        String countSql = "SELECT COUNT(*) FROM blog.post_tags WHERE post_id = :postId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", testPostId);
        Integer count = jdbcTemplate.queryForObject(countSql, params, Integer.class);
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Должен корректно обработать создание тега с уже существующим именем")
    void shouldHandleExistingTagName() {
        String insertTagSql = "INSERT INTO blog.tags (name) VALUES (:name) RETURNING id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", "java");
        Long existingId = jdbcTemplate.queryForObject(insertTagSql, params, Long.class);

        List<TagEntity> tags = tagRepository.getTags(List.of("java"));

        assertThat(tags).hasSize(1);
        assertThat(tags.getFirst().getId()).isEqualTo(existingId);
        assertThat(tags.getFirst().getName()).isEqualTo("java");
    }

    @Test
    @DisplayName("Должен вернуть теги в алфавитном порядке")
    void shouldReturnTagsInAlphabeticalOrder() {
        List<String> tagNames = List.of("spring", "java", "kotlin", "docker");
        List<TagEntity> tags = tagRepository.getTags(tagNames);
        testPost.setTags(tags);
        tagRepository.saveTagsAndPost(testPost);

        List<TagEntity> foundTags = tagRepository.findTagsByPostId(testPostId);

        assertThat(foundTags).hasSize(4);
        assertThat(foundTags).extracting("name")
                .containsExactly("docker", "java", "kotlin", "spring");
    }

    @Test
    @DisplayName("Должен вернуть пустой список при ошибке доступа к данным при поиске тегов по ID поста")
    void shouldReturnEmptyListWhenDataAccessExceptionOccurs() {
        Long nonExistentPostId = null;

        List<TagEntity> foundTags = tagRepository.findTagsByPostId(nonExistentPostId);

        assertThat(foundTags).isEmpty();
    }

    @Test
    @DisplayName("Должен корректно обработать много тегов для одного поста")
    void shouldHandleManyTagsForOnePost() {
        List<String> tagNames = List.of("tag1", "tag2", "tag3", "tag4", "tag5",
                "tag6", "tag7", "tag8", "tag9", "tag10");
        List<TagEntity> tags = tagRepository.getTags(tagNames);
        testPost.setTags(tags);

        tagRepository.saveTagsAndPost(testPost);

        String countSql = "SELECT COUNT(*) FROM blog.post_tags WHERE post_id = :postId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", testPostId);
        Integer count = jdbcTemplate.queryForObject(countSql, params, Integer.class);
        assertThat(count).isEqualTo(10);

        List<TagEntity> foundTags = tagRepository.findTagsByPostId(testPostId);
        assertThat(foundTags).hasSize(10);
    }

    @Test
    @DisplayName("Должен корректно обработать специальные символы в названиях тегов")
    void shouldHandleSpecialCharactersInTagNames() {
        List<String> tagNames = List.of("c++", "c#", "f#", "dot-net");

        List<TagEntity> tags = tagRepository.getTags(tagNames);
        testPost.setTags(tags);
        tagRepository.saveTagsAndPost(testPost);

        List<TagEntity> foundTags = tagRepository.findTagsByPostId(testPostId);
        assertThat(foundTags).hasSize(4);
        assertThat(foundTags).extracting("name")
                .containsExactlyInAnyOrder("c++", "c#", "f#", "dot-net");
    }

    @Test
    @DisplayName("Должен корректно обработать пустой список тегов при сохранении")
    void shouldHandleEmptyTagsListWhenSaving() {
        testPost.setTags(List.of());

        tagRepository.saveTagsAndPost(testPost);

        String countSql = "SELECT COUNT(*) FROM blog.post_tags WHERE post_id = :postId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", testPostId);
        Integer count = jdbcTemplate.queryForObject(countSql, params, Integer.class);
        assertThat(count).isZero();
    }

}
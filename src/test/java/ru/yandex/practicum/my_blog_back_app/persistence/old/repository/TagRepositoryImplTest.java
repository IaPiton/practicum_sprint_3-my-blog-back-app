package ru.yandex.practicum.my_blog_back_app.persistence.old.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yandex.practicum.my_blog_back_app.configuration.TestCommonConfiguration;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.PostEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.TagEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DisplayName("Тесты репозитория тегов")
class TagRepositoryImplTest extends TestCommonConfiguration {

    @Autowired
    private ru.yandex.practicum.my_blog_back_app.persistence.old.repository.TagRepositoryOld tagRepository;

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long testPostId;
    private PostEntity testPost;

    @BeforeEach
    void setUp() {
        transactionTemplate.execute(status -> {
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
        Long existingTagId = jdbcClient.sql("""
                INSERT INTO blog.tags (name) VALUES (:name) RETURNING id
                """)
                .param("name", "java")
                .query(Long.class)
                .single();

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

        Integer count = jdbcClient.sql("SELECT COUNT(*) FROM blog.post_tags WHERE post_id = :postId")
                .param("postId", testPostId)
                .query(Integer.class)
                .single();
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

        Integer count = jdbcClient.sql("SELECT COUNT(*) FROM blog.post_tags WHERE post_id = :postId")
                .param("postId", testPostId)
                .query(Integer.class)
                .single();
        assertThat(count).isEqualTo(2);
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

        Integer beforeCount = jdbcClient.sql("SELECT COUNT(*) FROM blog.post_tags WHERE post_id = :postId")
                .param("postId", testPostId)
                .query(Integer.class)
                .single();
        assertThat(beforeCount).isEqualTo(2);

        tagRepository.deleteTagAndPost(testPostId);

        Integer afterCount = jdbcClient.sql("SELECT COUNT(*) FROM blog.post_tags WHERE post_id = :postId")
                .param("postId", testPostId)
                .query(Integer.class)
                .single();
        assertThat(afterCount).isZero();

        Integer tagsCount = jdbcClient.sql("SELECT COUNT(*) FROM blog.tags")
                .query(Integer.class)
                .single();
        assertThat(tagsCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Должен корректно обработать удаление связей для поста без тегов")
    void shouldHandleDeleteForPostWithoutTags() {
        tagRepository.deleteTagAndPost(testPostId);

        Integer count = jdbcClient.sql("SELECT COUNT(*) FROM blog.post_tags WHERE post_id = :postId")
                .param("postId", testPostId)
                .query(Integer.class)
                .single();
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Должен корректно обработать создание тега с уже существующим именем")
    void shouldHandleExistingTagName() {
        Long existingId = jdbcClient.sql("""
                INSERT INTO blog.tags (name) VALUES (:name) RETURNING id
                """)
                .param("name", "java")
                .query(Long.class)
                .single();

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

        Integer count = jdbcClient.sql("SELECT COUNT(*) FROM blog.post_tags WHERE post_id = :postId")
                .param("postId", testPostId)
                .query(Integer.class)
                .single();
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

        Integer count = jdbcClient.sql("SELECT COUNT(*) FROM blog.post_tags WHERE post_id = :postId")
                .param("postId", testPostId)
                .query(Integer.class)
                .single();
        assertThat(count).isZero();
    }

}
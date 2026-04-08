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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DisplayName("Тесты репозитория постов")
class PostRepositoryImplTest extends TestCommonConfiguration {

    @Autowired
    private ru.yandex.practicum.my_blog_back_app.persistence.old.repository.PostRepositoryOld postRepository;

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private PostEntity testPost;
    private TagEntity testTag1;
    private TagEntity testTag2;

    @BeforeEach
    void setUp() {
        transactionTemplate.execute(status -> {
            jdbcClient.sql("DELETE FROM blog.post_tags").update();
            jdbcClient.sql("DELETE FROM blog.posts").update();
            jdbcClient.sql("DELETE FROM blog.tags").update();
            return null;
        });

        transactionTemplate.execute(status -> {
            Long tagId1 = jdbcClient.sql("""
                    INSERT INTO blog.tags (name) VALUES (:name) RETURNING id
                    """)
                    .param("name", "java")
                    .query(Long.class)
                    .single();

            Long tagId2 = jdbcClient.sql("""
                    INSERT INTO blog.tags (name) VALUES (:name) RETURNING id
                    """)
                    .param("name", "spring")
                    .query(Long.class)
                    .single();

            testTag1 = new TagEntity();
            testTag1.setId(tagId1);
            testTag1.setName("java");

            testTag2 = new TagEntity();
            testTag2.setId(tagId2);
            testTag2.setName("spring");

            return null;
        });

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

        Integer count = jdbcClient.sql("SELECT COUNT(*) FROM blog.post_tags WHERE post_id = :postId")
                .param("postId", savedPost.getId())
                .query(Integer.class)
                .single();
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Должен найти пост по ID")
    void shouldFindPostById() {
        PostEntity savedPost = postRepository.savePost(testPost);

        Optional<PostEntity> foundPost = postRepository.findById(savedPost.getId());

        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getId()).isEqualTo(savedPost.getId());
        assertThat(foundPost.get().getTitle()).isEqualTo("Тестовый пост");
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
        PostEntity savedPost = postRepository.savePost(testPost);
        jdbcClient.sql("""
            INSERT INTO blog.post_tags(post_id, tag_id) VALUES (:postId, :tagId)
            """)
                .param("postId", savedPost.getId())
                .param("tagId", testTag1.getId())
                .update();

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
        PostEntity savedTestPost = postRepository.savePost(testPost);
        jdbcClient.sql("""
            INSERT INTO blog.post_tags(post_id, tag_id) VALUES (:postId, :tagId)
            """)
                .param("postId", savedTestPost.getId())
                .param("tagId", testTag1.getId())
                .update();
        jdbcClient.sql("""
            INSERT INTO blog.post_tags(post_id, tag_id) VALUES (:postId, :tagId)
            """)
                .param("postId", savedTestPost.getId())
                .param("tagId", testTag2.getId())
                .update();

        transactionTemplate.execute(status -> {
            PostEntity postWithOnlyJava = new PostEntity();
            postWithOnlyJava.setTitle("Только Java");
            postWithOnlyJava.setText("Содержание");
            postWithOnlyJava.setLikesCount(0L);
            PostEntity saved = postRepository.savePost(postWithOnlyJava);

            jdbcClient.sql("""
                INSERT INTO blog.post_tags(post_id, tag_id) VALUES (:postId, :tagId)
                """)
                    .param("postId", saved.getId())
                    .param("tagId", testTag1.getId())
                    .update();
            return null;
        });

        transactionTemplate.execute(status -> {
            PostEntity postWithOnlySpring = new PostEntity();
            postWithOnlySpring.setTitle("Только Spring");
            postWithOnlySpring.setText("Содержание");
            postWithOnlySpring.setLikesCount(0L);
            PostEntity saved = postRepository.savePost(postWithOnlySpring);

            jdbcClient.sql("""
                INSERT INTO blog.post_tags(post_id, tag_id) VALUES (:postId, :tagId)
                """)
                    .param("postId", saved.getId())
                    .param("tagId", testTag2.getId())
                    .update();
            return null;
        });

        int count = postRepository.countPostsWithFilters(null, List.of("java"));

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Должен подсчитать посты с фильтрацией по нескольким тегам (AND логика)")
    void shouldCountPostsWithMultipleTagsAndLogic() {
        PostEntity savedPost = postRepository.savePost(testPost);

        jdbcClient.sql("""
            INSERT INTO blog.post_tags(post_id, tag_id) VALUES (:postId, :tagId)
            """)
                .param("postId", savedPost.getId())
                .param("tagId", testTag1.getId())
                .update();

        jdbcClient.sql("""
            INSERT INTO blog.post_tags(post_id, tag_id) VALUES (:postId, :tagId)
            """)
                .param("postId", savedPost.getId())
                .param("tagId", testTag2.getId())
                .update();

        transactionTemplate.execute(status -> {
            PostEntity postWithOnlyJava = new PostEntity();
            postWithOnlyJava.setTitle("Только Java");
            postWithOnlyJava.setText("Содержание");
            postWithOnlyJava.setLikesCount(0L);
            PostEntity saved = postRepository.savePost(postWithOnlyJava);

            jdbcClient.sql("""
                INSERT INTO blog.post_tags(post_id, tag_id) VALUES (:postId, :tagId)
                """)
                    .param("postId", saved.getId())
                    .param("tagId", testTag1.getId())
                    .update();
            return null;
        });

        transactionTemplate.execute(status -> {
            PostEntity postWithOnlySpring = new PostEntity();
            postWithOnlySpring.setTitle("Только Spring");
            postWithOnlySpring.setText("Содержание");
            postWithOnlySpring.setLikesCount(0L);
            PostEntity saved = postRepository.savePost(postWithOnlySpring);

            jdbcClient.sql("""
                INSERT INTO blog.post_tags(post_id, tag_id) VALUES (:postId, :tagId)
                """)
                    .param("postId", saved.getId())
                    .param("tagId", testTag2.getId())
                    .update();
            return null;
        });

        int count = postRepository.countPostsWithFilters(null, List.of("java", "spring"));

        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Должен подсчитать посты с фильтрацией по заголовку и тегам одновременно")
    void shouldCountPostsWithTitleAndTagFilters() {
        PostEntity savedTestPost = postRepository.savePost(testPost);
        jdbcClient.sql("""
            INSERT INTO blog.post_tags(post_id, tag_id) VALUES (:postId, :tagId)
            """)
                .param("postId", savedTestPost.getId())
                .param("tagId", testTag1.getId())
                .update();

        PostEntity anotherPost = new PostEntity();
        anotherPost.setTitle("Тестовый пост без тегов");
        anotherPost.setText("Содержание");
        anotherPost.setLikesCount(0L);
        postRepository.savePost(anotherPost);

        transactionTemplate.execute(status -> {
            PostEntity postWithDifferentTitle = new PostEntity();
            postWithDifferentTitle.setTitle("Другой пост");
            postWithDifferentTitle.setText("Содержание");
            postWithDifferentTitle.setLikesCount(0L);
            PostEntity saved = postRepository.savePost(postWithDifferentTitle);

            jdbcClient.sql("""
                INSERT INTO blog.post_tags(post_id, tag_id) VALUES (:postId, :tagId)
                """)
                    .param("postId", saved.getId())
                    .param("tagId", testTag1.getId())
                    .update();
            return null;
        });

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
        PostEntity savedPost = postRepository.savePost(testPost);
        jdbcClient.sql("""
            INSERT INTO blog.post_tags(post_id, tag_id) VALUES (:postId, :tagId)
            """)
                .param("postId", savedPost.getId())
                .param("tagId", testTag1.getId())
                .update();

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
        PostEntity savedTestPost = postRepository.savePost(testPost);
        jdbcClient.sql("""
            INSERT INTO blog.post_tags(post_id, tag_id) VALUES (:postId, :tagId)
            """)
                .param("postId", savedTestPost.getId())
                .param("tagId", testTag1.getId())
                .update();

        transactionTemplate.execute(status -> {
            PostEntity anotherPostWithJava = new PostEntity();
            anotherPostWithJava.setTitle("Еще один пост с Java");
            anotherPostWithJava.setText("Содержание");
            anotherPostWithJava.setLikesCount(0L);
            PostEntity saved = postRepository.savePost(anotherPostWithJava);

            jdbcClient.sql("""
                INSERT INTO blog.post_tags(post_id, tag_id) VALUES (:postId, :tagId)
                """)
                    .param("postId", saved.getId())
                    .param("tagId", testTag1.getId())
                    .update();
            return null;
        });

        transactionTemplate.execute(status -> {
            PostEntity postWithoutJava = new PostEntity();
            postWithoutJava.setTitle("Пост без Java");
            postWithoutJava.setText("Содержание");
            postWithoutJava.setLikesCount(0L);
            PostEntity saved = postRepository.savePost(postWithoutJava);

            jdbcClient.sql("""
                INSERT INTO blog.post_tags(post_id, tag_id) VALUES (:postId, :tagId)
                """)
                    .param("postId", saved.getId())
                    .param("tagId", testTag2.getId())
                    .update();
            return null;
        });

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
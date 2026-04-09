package ru.yandex.practicum.my_blog_back_app.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.PostEntity;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {
    @Query(value = """
            SELECT DISTINCT p.*
            FROM blog.posts p
            WHERE (:titleSubstring IS NULL OR :titleSubstring = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :titleSubstring, '%')))
            ORDER BY p.create_at DESC
            OFFSET :offset LIMIT :limit
            """, nativeQuery = true)
    List<PostEntity> findPostsWithFiltersNoTags(
            @Param("titleSubstring") String titleSubstring,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    @Query(value = """
            SELECT DISTINCT
               p.*
            FROM blog.posts p
            LEFT JOIN blog.post_tags pt ON p.id = pt.post_id
            LEFT JOIN blog.tags t ON pt.tag_id = t.id
            WHERE (:titleSubstring IS NULL OR :titleSubstring = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :titleSubstring, '%')))
            AND LOWER(t.name) IN (LOWER(:tags))
            ORDER BY p.create_at DESC
            OFFSET :offset LIMIT :limit
            """, nativeQuery = true)
    List<PostEntity> findPostsWithFiltersWithTags(
            @Param("titleSubstring") String titleSubstring,
            @Param("tags") List<String> tags,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    @Query(value = """
            SELECT
              COUNT(p.*)
            FROM blog.posts p
            WHERE (:titleSubstring IS NULL OR :titleSubstring = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :titleSubstring, '%')))
            """, nativeQuery = true)
    Integer findCountPostsWithFiltersNoTags(
            @Param("titleSubstring") String titleSubstring
    );

    @Query(value = """
            SELECT
               COUNT(p.*)
            FROM blog.posts p
            LEFT JOIN blog.post_tags pt ON p.id = pt.post_id
            LEFT JOIN blog.tags t ON pt.tag_id = t.id
            WHERE (:titleSubstring IS NULL OR :titleSubstring = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :titleSubstring, '%')))
            AND LOWER(t.name) IN (LOWER(:tags))
            """, nativeQuery = true)
    Integer findCountPostsWithFiltersWithTags(
            @Param("titleSubstring") String titleSubstring,
            @Param("tags") List<String> tags
    );
}
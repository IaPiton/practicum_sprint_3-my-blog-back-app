package ru.yandex.practicum.my_blog_back_app.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.PostEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {
    private final JdbcClient jdbcClient;

    @Override
    public PostEntity savePost(PostEntity postEntity) {
        String postSql = """
                INSERT INTO blog.posts(title, text, likes_count, image, create_at, update_at)
                 VALUES (:title, :text, :likesCount, :image, :createAt, :updateAt)
                 RETURNING id, title, text, likes_count, image, create_at, update_at;
                """;

        LocalDateTime now = LocalDateTime.now();

        return jdbcClient.sql(postSql)
                .param("title", postEntity.getTitle())
                .param("text", postEntity.getText())
                .param("likesCount", postEntity.getLikesCount() != null ? postEntity.getLikesCount() : 0)
                .param("image", postEntity.getImage())
                .param("createAt", now)
                .param("updateAt", now)
                .query(PostEntity.class)
                .single();
    }

    @Override
    public List<PostEntity> findPostsWithFilters(String titleSubstring, List<String> tags, int limit, int offset) {
        StringBuilder sql = new StringBuilder("""
            SELECT DISTINCT p.*
            FROM blog.posts p
            LEFT JOIN blog.post_tags pt ON p.id = pt.post_id
            LEFT JOIN blog.tags t ON pt.tag_id = t.id
            WHERE 1=1
            """);

        if (titleSubstring != null && !titleSubstring.isEmpty()) {
            sql.append("AND LOWER(p.title) LIKE :titleSubstring ");
        }

        if (tags != null && !tags.isEmpty()) {
            for (int i = 0; i < tags.size(); i++) {
                sql.append("AND EXISTS (")
                        .append("SELECT 1 FROM blog.post_tags pt2 ")
                        .append("JOIN blog.tags t2 ON pt2.tag_id = t2.id ")
                        .append("WHERE pt2.post_id = p.id ")
                        .append("AND LOWER(t2.name) = LOWER(:tag").append(i).append(")")
                        .append(") ");
            }
        }

        sql.append("ORDER BY p.create_at DESC LIMIT :limit OFFSET :offset");

        var spec = jdbcClient.sql(sql.toString());

        if (titleSubstring != null && !titleSubstring.isEmpty()) {
            spec.param("titleSubstring", "%" + titleSubstring.toLowerCase() + "%");
        }

        if (tags != null && !tags.isEmpty()) {
            for (int i = 0; i < tags.size(); i++) {
                spec.param("tag" + i, tags.get(i));
            }
        }

        spec.param("limit", limit)
                .param("offset", offset);

        return spec.query(PostEntity.class).list();
    }

    @Override
    public int countPostsWithFilters(String titleSubstring, List<String> tags) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(DISTINCT p.id)
            FROM blog.posts p
            LEFT JOIN blog.post_tags pt ON p.id = pt.post_id
            LEFT JOIN blog.tags t ON pt.tag_id = t.id
            WHERE 1=1
            """);

        if (titleSubstring != null && !titleSubstring.isEmpty()) {
            sql.append("AND LOWER(p.title) LIKE :titleSubstring ");
        }

        if (tags != null && !tags.isEmpty()) {
            for (int i = 0; i < tags.size(); i++) {
                sql.append("AND EXISTS (")
                        .append("SELECT 1 FROM blog.post_tags pt2 ")
                        .append("JOIN blog.tags t2 ON pt2.tag_id = t2.id ")
                        .append("WHERE pt2.post_id = p.id ")
                        .append("AND LOWER(t2.name) = LOWER(:tag").append(i).append(")")
                        .append(") ");
            }
        }

        var spec = jdbcClient.sql(sql.toString());

        if (titleSubstring != null && !titleSubstring.isEmpty()) {
            spec.param("titleSubstring", "%" + titleSubstring.toLowerCase() + "%");
        }

        if (tags != null && !tags.isEmpty()) {
            for (int i = 0; i < tags.size(); i++) {
                spec.param("tag" + i, tags.get(i));
            }
        }

        return spec.query(Integer.class).optional().orElse(0);
    }

    @Override
    public Optional<PostEntity> findById(Long postId) {
        String sql = """
                SELECT * FROM blog.posts
                WHERE id = :id;
                """;

        return jdbcClient.sql(sql)
                .param("id", postId)
                .query(PostEntity.class)
                .optional();
    }

    @Override
    public void update(PostEntity postEntity) {
        String postSql = """
                UPDATE blog.posts
                SET title = :title,
                    text = :text,
                    likes_count = :likesCount,
                    image = :image,
                    update_at = :updateAt
                WHERE id = :id
                """;

        LocalDateTime now = LocalDateTime.now();

        jdbcClient.sql(postSql)
                .param("id", postEntity.getId())
                .param("title", postEntity.getTitle())
                .param("text", postEntity.getText())
                .param("likesCount", postEntity.getLikesCount() != null ? postEntity.getLikesCount() : 0)
                .param("image", postEntity.getImage())
                .param("updateAt", now)
                .update();
    }

    @Override
    public void delete(Long postId) {
        String sql = """
                DELETE FROM blog.posts
                WHERE id = :postId
                """;

        jdbcClient.sql(sql)
                .param("postId", postId)
                .update();
    }
}
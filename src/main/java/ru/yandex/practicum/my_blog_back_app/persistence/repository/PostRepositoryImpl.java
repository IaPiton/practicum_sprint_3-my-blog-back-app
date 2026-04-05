package ru.yandex.practicum.my_blog_back_app.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.PostEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TagRepository tagRepository;


    private final RowMapper<PostEntity> postRowMapper = (rs, rowNum) -> {
        PostEntity post = new PostEntity();
        post.setId(rs.getLong("id"));
        post.setTitle(rs.getString("title"));
        post.setText(rs.getString("text"));
        post.setLikesCount(rs.getLong("likes_count"));
        post.setImage(rs.getBytes("image"));
        post.setCreateAt(rs.getTimestamp("create_at").toLocalDateTime());
        post.setUpdateAt(rs.getTimestamp("update_at").toLocalDateTime());
        return post;
    };

    @Override
    public PostEntity savePost(PostEntity postEntity) {
        String postSql = """
                INSERT INTO blog.posts(title, text, likes_count, image, create_at, update_at)
                VALUES (:title, :text, :likesCount, :image, :createAt,  :updateAt)
                RETURNING id;
                """;

        LocalDateTime now = LocalDateTime.now();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("title", postEntity.getTitle());
        params.addValue("text", postEntity.getText());

        params.addValue("likesCount", postEntity.getLikesCount() != null ? postEntity.getLikesCount() : 0);
        params.addValue("image", postEntity.getImage());
        params.addValue("createAt", now);
        params.addValue("updateAt", now);

        Long postId = jdbcTemplate.queryForObject(postSql, params, Long.class);

        postEntity.setId(postId);
        postEntity.setCreateAt(now);
        postEntity.setUpdateAt(now);

        if (postEntity.getTags() != null && !postEntity.getTags().isEmpty()) {
            tagRepository.saveTagsAndPost(postEntity);
        }

        return postEntity;
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

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (titleSubstring != null && !titleSubstring.isEmpty()) {
            sql.append("AND LOWER(p.title) LIKE :titleSubstring ");
            params.addValue("titleSubstring", "%" + titleSubstring.toLowerCase() + "%");
        }

        if (tags != null && !tags.isEmpty()) {
            for (int i = 0; i < tags.size(); i++) {
                String tagParam = "tag" + i;
                sql.append("AND EXISTS (")
                        .append("SELECT 1 FROM blog.post_tags pt2 ")
                        .append("JOIN blog.tags t2 ON pt2.tag_id = t2.id ")
                        .append("WHERE pt2.post_id = p.id ").append("AND LOWER(t2.name) = LOWER(:").append(tagParam).append(")")
                        .append(") ");
                params.addValue(tagParam, tags.get(i));
            }
        }

        sql.append("ORDER BY p.create_at DESC LIMIT :limit OFFSET :offset");
        params.addValue("limit", limit);
        params.addValue("offset", offset);

        return jdbcTemplate.query(sql.toString(), params, postRowMapper)
                .stream()
                .peek(post -> post.setTags(tagRepository.findTagsByPostId(post.getId())))
                .toList();
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

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (titleSubstring != null && !titleSubstring.isEmpty()) {
            sql.append("AND LOWER(p.title) LIKE :titleSubstring ");
            params.addValue("titleSubstring", "%" + titleSubstring.toLowerCase() + "%");
        }

        if (tags != null && !tags.isEmpty()) {
            for (int i = 0; i < tags.size(); i++) {
                String tagParam = "tag" + i;
                sql.append("AND EXISTS (")
                        .append("SELECT 1 FROM blog.post_tags pt2 ")
                        .append("JOIN blog.tags t2 ON pt2.tag_id = t2.id ")
                        .append("WHERE pt2.post_id = p.id ").append("AND LOWER(t2.name) = LOWER(:").append(tagParam).append(")")
                        .append(") ");
                params.addValue(tagParam, tags.get(i));
            }
        }

        Integer count = jdbcTemplate.queryForObject(sql.toString(), params, Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public Optional<PostEntity> findById(Long postId) {
        String sql = """
                SELECT * FROM blog.posts
                WHERE id = :id;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", postId);

        try {
            PostEntity postEntity = jdbcTemplate.queryForObject(sql, params, postRowMapper);
            if (postEntity != null) {
                postEntity.setTags(tagRepository.findTagsByPostId(postId));
            }
            return Optional.ofNullable(postEntity);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
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

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", postEntity.getId());
        params.addValue("title", postEntity.getTitle());
        params.addValue("text", postEntity.getText());
        params.addValue("likesCount", postEntity.getLikesCount() != null ? postEntity.getLikesCount() : 0);
        params.addValue("image", postEntity.getImage());
        params.addValue("updateAt", now);

        tagRepository.saveTagsAndPost(postEntity);

        jdbcTemplate.update(postSql, params);

    }

    @Override
    public void delete(Long postId) {
        String sql = """
                DELETE FROM blog.posts
                WHERE id = :postId
                """;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", postId);

        jdbcTemplate.update(sql, params);
    }
}
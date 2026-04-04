package ru.yandex.practicum.my_blog_back_app.persistence.repository;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.CommentsEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Component
public class CommentRepositoryImpl implements CommentRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CommentRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<CommentsEntity> commentRowMapper = (rs, rowNum) -> {
        CommentsEntity commentsEntity = new CommentsEntity();
        commentsEntity.setId(rs.getLong("id"));
        commentsEntity.setText(rs.getString("text"));
        commentsEntity.setPostId(rs.getLong("post_id"));
        commentsEntity.setCreateAt(rs.getTimestamp("create_at").toLocalDateTime());
        commentsEntity.setUpdateAt(rs.getTimestamp("update_at").toLocalDateTime());
        return commentsEntity;
    };

    @Override
    public Long countCommentsByPost(Long postId) {
        String sql = """
                SELECT COUNT(*)
                FROM blog.comments c
                WHERE c.post_id = :postId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", postId);

        Long count = jdbcTemplate.queryForObject(sql, params, Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public boolean postExists(Long postId) {
        String sql = """
                SELECT COUNT(*) FROM blog.posts WHERE id = :postId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", postId);

        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public List<CommentsEntity> findCommentsByPostId(Long postId) {
        String sql = """
                SELECT id, text, post_id, create_at, update_at
                FROM blog.comments
                WHERE post_id = :postId
                ORDER BY create_at ASC
                """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", postId);

        try {
            return jdbcTemplate.query(sql, params, commentRowMapper);
        } catch (DataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public Long save(CommentsEntity commentsEntity) {
        String commentSql = """
                INSERT INTO blog.comments(text, post_id, create_at, update_at)
                VALUES (:text, :postId, :createAt, :updateAt)
                RETURNING id;
                """;

        LocalDateTime now = LocalDateTime.now();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("text", commentsEntity.getText());
        params.addValue("postId", commentsEntity.getPostId());
        params.addValue("createAt", now);
        params.addValue("updateAt", now);

        return jdbcTemplate.queryForObject(commentSql, params, Long.class);
    }

    @Override
    public CommentsEntity findById(Long commentId) {
        String sql = """
                SELECT * FROM blog.comments
                WHERE id = :id;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", commentId);

        return jdbcTemplate.queryForObject(sql, params, commentRowMapper);
    }

    @Override
    public void update(CommentsEntity commentsEntity) {
         String postSql = """
                UPDATE blog.comments
                SET text = :text,
                    update_at = :updateAt
                WHERE id = :id
                """;

        LocalDateTime now = LocalDateTime.now();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", commentsEntity.getId());
        params.addValue("text", commentsEntity.getText());
        params.addValue("updateAt", now);

        jdbcTemplate.update(postSql, params);
    }

    @Override
    public void deleteByCommentId(Long commentId) {
        String sql = """
                DELETE FROM blog.comments
                WHERE id = :id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", commentId);

        jdbcTemplate.update(sql, params);
    }

    @Override
    public void deleteByPostId(Long postId) {
        String sql = """
                DELETE FROM blog.comments
                WHERE post_id = :postId
                """;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", postId);

        jdbcTemplate.update(sql, params);
    }

}
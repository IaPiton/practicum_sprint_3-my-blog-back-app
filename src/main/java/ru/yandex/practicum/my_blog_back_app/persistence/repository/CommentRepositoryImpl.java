package ru.yandex.practicum.my_blog_back_app.persistence.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CommentRepositoryImpl implements CommentRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    public CommentRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


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
}

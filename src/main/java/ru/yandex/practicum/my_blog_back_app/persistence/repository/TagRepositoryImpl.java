package ru.yandex.practicum.my_blog_back_app.persistence.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.PostEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.TagEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class TagRepositoryImpl implements TagRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public TagRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<TagEntity> tagRowMapper = (rs, rowNum) -> {
        TagEntity tagEntity = new TagEntity();
        tagEntity.setId(rs.getLong("id"));
        tagEntity.setName(rs.getString("name"));
        return tagEntity;
    };

    @Override
    public List<TagEntity> getTags(List<String> tags) {
        return tags.stream()
                .map(String::trim)
                .map(tagName -> findTagByName(tagName)
                        .orElseGet(() -> saveTag(tagName)))
                .toList();
    }

    @Override
    public void saveTagsAndPost(PostEntity postEntity) {
        String tagSql = """
                INSERT INTO blog.post_tags(post_id, tag_id)
                VALUES (:postId, :tagId)
                ON CONFLICT (post_id, tag_id) DO NOTHING;
                """;

        MapSqlParameterSource[] batchParams = postEntity.getTags().stream()
                .map(tag -> new MapSqlParameterSource()
                        .addValue("postId", postEntity.getId())
                        .addValue("tagId", tag.getId()))
                .toArray(MapSqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(tagSql, batchParams);
    }

    @Override
    public List<TagEntity> findTagsByPostId(Long postId) {
        String sql = """
                SELECT t.id, t.name
                FROM blog.tags t
                JOIN blog.post_tags pt ON t.id = pt.tag_id
                WHERE pt.post_id = :postId
                ORDER BY t.name
                """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", postId);

        try {
            return jdbcTemplate.query(sql, params, tagRowMapper);
        } catch (DataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void deleteTagAndPost(Long postId) {
        String sql = """
                DELETE FROM blog.post_tags
                WHERE post_id = :postId
                """;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("postId", postId);

        jdbcTemplate.update(sql, params);
    }

    private TagEntity saveTag(String tag) {
        String sql = """
                INSERT INTO blog.tags(name)
                VALUES (:name)
                ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
                RETURNING id;
                """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", tag);

        Long id = jdbcTemplate.queryForObject(sql, params, Long.class);

        TagEntity tagEntity = new TagEntity();
        tagEntity.setId(id);
        tagEntity.setName(tag);
        return tagEntity;
    }

    private Optional<TagEntity> findTagByName(String tag) {
        String sql = """
                SELECT * FROM blog.tags t
                WHERE t.name = :name;
                """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", tag);

        try {
            TagEntity tagEntity = jdbcTemplate.queryForObject(sql, params, tagRowMapper);
            return Optional.ofNullable(tagEntity);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}

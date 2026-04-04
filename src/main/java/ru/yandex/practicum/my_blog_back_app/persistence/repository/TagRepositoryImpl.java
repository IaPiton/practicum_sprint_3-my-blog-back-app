package ru.yandex.practicum.my_blog_back_app.persistence.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.PostEntity;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.TagEntity;

import java.util.List;
import java.util.Optional;

@Component
public class TagRepositoryImpl implements TagRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TagRepositoryImpl(JdbcTemplate jdbcTemplate) {
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
                VALUES (?, ?)
                ON CONFLICT (post_id, tag_id) DO NOTHING;
                """;

        for (TagEntity tag : postEntity.getTags()) {
            jdbcTemplate.update(tagSql, postEntity.getId(), tag.getId());
        }
    }

    private TagEntity saveTag(String tag) {
        TagEntity tagEntity = new TagEntity();

        String sql = """
            INSERT INTO blog.tags(name)
                VALUES(?)
                RETURNING id;
            """;

        Long id = jdbcTemplate.queryForObject(sql, Long.class, tag);
        tagEntity.setId(id);
        tagEntity.setName(tag);
        return tagEntity;
    }

    private Optional<TagEntity> findTagByName(String tag) {
        String sql = """
                SELECT * FROM blog.tags t
                    WHERE t.name = ?;
                """;
        try {
            TagEntity tagEntity = jdbcTemplate.queryForObject(sql, tagRowMapper, tag);
            return Optional.ofNullable(tagEntity);
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }
}

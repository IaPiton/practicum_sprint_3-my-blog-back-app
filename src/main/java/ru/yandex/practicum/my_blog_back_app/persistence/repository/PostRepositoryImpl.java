package ru.yandex.practicum.my_blog_back_app.persistence.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.PostEntity;

import java.time.LocalDateTime;

@Component
public class PostRepositoryImpl implements PostRepository {
    private final JdbcTemplate jdbcTemplate;
    private final TagRepository tagRepository;

    @Autowired
    public PostRepositoryImpl(JdbcTemplate jdbcTemplate,
                              TagRepository tagRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.tagRepository = tagRepository;
    }

    @Override
    public PostEntity savePost(PostEntity postEntity) {
        String postSql = """
            INSERT INTO blog.posts(title, text, likes_count, create_at, update_at)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id;
            """;

        LocalDateTime now = LocalDateTime.now();
        Long postId = jdbcTemplate.queryForObject(
                postSql,
                Long.class,
                postEntity.getTitle(),
                postEntity.getText(),
                postEntity.getLikesCount(),
                now,
                now
        );

        postEntity.setId(postId);
        postEntity.setCreateAt(now);
        postEntity.setUpdateAt(now);

        if (postEntity.getTags() != null && !postEntity.getTags().isEmpty()) {
            tagRepository.saveTagsAndPost(postEntity);
        }

        return postEntity;
    }
}
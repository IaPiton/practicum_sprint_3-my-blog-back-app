package ru.yandex.practicum.my_blog_back_app.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.yandex.practicum.my_blog_back_app.persistence.repository.*;

@Configuration
public class TestConfiguration {
    @Bean
    public CommentRepository commentRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        return new CommentRepositoryImpl(jdbcTemplate);
    }

    @Bean
    public TagRepository tagRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        return new TagRepositoryImpl(jdbcTemplate);
    }

    @Bean
    public PostRepository postRepository(NamedParameterJdbcTemplate jdbcTemplate,
                                         TagRepositoryImpl tagRepository) {
        return new PostRepositoryImpl(jdbcTemplate, tagRepository);
    }
}
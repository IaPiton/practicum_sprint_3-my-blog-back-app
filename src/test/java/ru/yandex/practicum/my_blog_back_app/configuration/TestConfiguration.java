package ru.yandex.practicum.my_blog_back_app.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import ru.yandex.practicum.my_blog_back_app.persistence.repository.*;

@Configuration
public class TestConfiguration {
    @Bean
    public CommentRepository commentRepository(JdbcClient jdbcClient) {
        return new CommentRepositoryImpl(jdbcClient);
    }

    @Bean
    public TagRepository tagRepository(JdbcClient jdbcClient) {
        return new TagRepositoryImpl(jdbcClient);
    }

    @Bean
    public PostRepository postRepository(JdbcClient jdbcClient) {
        return new PostRepositoryImpl(jdbcClient);
    }
}
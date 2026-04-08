package ru.yandex.practicum.my_blog_back_app.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.CommentsEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

//@Component
//@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepository {
//    private final JdbcClient jdbcClient;
//
//    @Override
//    public Long countCommentsByPost(Long postId) {
//        String sql = """
//                SELECT COUNT(*)
//                FROM blog.comments c
//                WHERE c.post_id = :postId
//                """;
//
//        return jdbcClient.sql(sql)
//                .param("postId", postId)
//                .query(Long.class)
//                .optional()
//                .orElse(0L);
//    }
//
//    @Override
//    public boolean postExists(Long postId) {
//        String sql = """
//                SELECT COUNT(*) FROM blog.posts WHERE id = :postId
//                """;
//
//        Integer count = jdbcClient.sql(sql)
//                .param("postId", postId)
//                .query(Integer.class)
//                .optional()
//                .orElse(0);
//
//        return count > 0;
//    }
//
//    @Override
//    public List<CommentsEntity> findCommentsByPostId(Long postId) {
//        String sql = """
//                SELECT id, text, post_id, create_at, update_at
//                FROM blog.comments
//                WHERE post_id = :postId
//                ORDER BY create_at ASC
//                """;
//
//        return jdbcClient.sql(sql)
//                .param("postId", postId)
//                .query(CommentsEntity.class)
//                .list()
//                .stream()
//                .filter(Objects::nonNull)
//                .toList();
//    }
//
//    @Override
//    public Long save(CommentsEntity commentsEntity) {
//        String commentSql = """
//                INSERT INTO blog.comments(text, post_id, create_at, update_at)
//                VALUES (:text, :postId, :createAt, :updateAt)
//                RETURNING id;
//                """;
//
//        LocalDateTime now = LocalDateTime.now();
//
//        return jdbcClient.sql(commentSql)
//                .param("text", commentsEntity.getText())
//                .param("postId", commentsEntity.getPostId())
//                .param("createAt", now)
//                .param("updateAt", now)
//                .query(Long.class)
//                .optional()
//                .orElseThrow(() -> new RuntimeException("Ошибка при сохранении комментария"));
//    }
//
//    @Override
//    public Optional<CommentsEntity> findById(Long commentId) {
//        String sql = """
//                SELECT * FROM blog.comments
//                WHERE id = :id;
//                """;
//
//        return jdbcClient.sql(sql)
//                .param("id", commentId)
//                .query(CommentsEntity.class)
//                .optional();
//    }
//
//    @Override
//    public void update(CommentsEntity commentsEntity) {
//         String postSql = """
//                UPDATE blog.comments
//                SET text = :text,
//                    update_at = :updateAt
//                WHERE id = :id
//                """;
//
//        LocalDateTime now = LocalDateTime.now();
//
//        jdbcClient.sql(postSql)
//                .param("id", commentsEntity.getId())
//                .param("text", commentsEntity.getText())
//                .param("updateAt", now)
//                .update();
//    }
//
//    @Override
//    public void deleteByCommentId(Long commentId) {
//        String sql = """
//                DELETE FROM blog.comments
//                WHERE id = :id
//                """;
//        MapSqlParameterSource params = new MapSqlParameterSource();
//        params.addValue("id", commentId);
//
//        jdbcClient.sql(sql)
//                .param("id", commentId)
//                .update();
//    }
//
//    @Override
//    public void deleteByPostId(Long postId) {
//        String sql = """
//                DELETE FROM blog.comments
//                WHERE post_id = :postId
//                """;
//
//        jdbcClient.sql(sql)
//                .param("postId", postId)
//                .update();
//    }

}
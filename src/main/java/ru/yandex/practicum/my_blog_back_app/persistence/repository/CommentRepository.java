package ru.yandex.practicum.my_blog_back_app.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.CommentEntity;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    @Query(value = "SELECT COUNT(*) FROM blog.comments WHERE post_id = :postId",
            nativeQuery = true)
    Long countCommentsByPost(@Param("postId") Long postId);

    List<CommentEntity> findCommentsByPostId(Long postId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM blog.posts WHERE id = :postId)",
            nativeQuery = true)
    boolean existsByPostId(Long postId);
}

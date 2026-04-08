package ru.yandex.practicum.my_blog_back_app.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.CommentEntity;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    @Query(value = "SELECT COUNT(*) FROM blog.comments WHERE post_id = :postId",
            nativeQuery = true)
    Long countCommentsByPost(@Param("postId") Long postId);
}

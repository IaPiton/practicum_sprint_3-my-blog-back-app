package ru.yandex.practicum.my_blog_back_app.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "comments", schema = "blog")
public class CommentsEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "text", nullable = false, columnDefinition = "TEXT")
        private String text;

        @Column(name = "post_id", nullable = false)
        private Long postId;

        @Column(name = "create_at", nullable = false, updatable = false)
        private LocalDateTime createAt;

        @Column(name = "update_at", nullable = false)
        private LocalDateTime updateAt;
}
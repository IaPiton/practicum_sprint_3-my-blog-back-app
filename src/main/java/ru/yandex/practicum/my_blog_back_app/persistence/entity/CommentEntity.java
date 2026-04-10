package ru.yandex.practicum.my_blog_back_app.persistence.entity;

import jakarta.persistence.*;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments", schema = "blog")
public class CommentEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, columnDefinition = "TEXT")
        private String text;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "post_id", nullable = false)
        private PostEntity post;

        @CreationTimestamp
        @Column(name = "create_at", nullable = false, updatable = false)
        private LocalDateTime createAt;

        @UpdateTimestamp
        @Column(name = "update_at", nullable = false)
        private LocalDateTime updateAt;
}
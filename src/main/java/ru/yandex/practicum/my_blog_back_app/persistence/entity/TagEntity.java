package ru.yandex.practicum.my_blog_back_app.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Setter
@Getter
@Table(name = "tags", schema = "blog")
public class TagEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "name", nullable = false, unique = true, length = 100)
        private String name;

        @Column(name = "post_id")
        private Long postId;
}
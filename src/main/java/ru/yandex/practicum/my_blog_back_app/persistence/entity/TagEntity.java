package ru.yandex.practicum.my_blog_back_app.persistence.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tags", schema = "blog")
public class TagEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "name", nullable = false, unique = true, length = 100)
        private String name;

        @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
        private List<PostEntity> posts = new ArrayList<>();
}
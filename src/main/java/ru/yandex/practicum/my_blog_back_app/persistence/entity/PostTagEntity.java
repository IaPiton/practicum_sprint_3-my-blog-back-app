package ru.yandex.practicum.my_blog_back_app.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "post_tags", schema = "blog")
@IdClass(PostTagEntity.PostTagId.class)
public class PostTagEntity {

        @Id
        @Column(name = "post_id")
        private Long postId;

        @Id
        @Column(name = "tag_id")
        private Long tagId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "post_id", insertable = false, updatable = false)
        private PostEntity postEntity;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "tag_id", insertable = false, updatable = false)
        private TagEntity tagEntity;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PostTagId implements Serializable {
                private Long postId;
                private Long tagId;
        }
}
package ru.yandex.practicum.my_blog_back_app.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.my_blog_back_app.persistence.entity.TagEntity;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, Long> {
    Optional<TagEntity> findByName(String tag);
}

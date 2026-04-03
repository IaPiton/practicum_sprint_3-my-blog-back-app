CREATE SCHEMA blog;

-- Таблица постов
CREATE TABLE blog.posts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    text TEXT NOT NULL,
    likes_count BIGINT NOT NULL DEFAULT 0,
    image BYTEA,
    create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--Таблица тегов
CREATE TABLE blog.tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

--Связь постов с тегами
CREATE TABLE blog.post_tags (
    post_id BIGINT NOT NULL REFERENCES blog.posts(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES blog.tags(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, tag_id)
);

--Таблица комментариев
CREATE TABLE blog.comments (
    id BIGSERIAL PRIMARY KEY,
    text TEXT NOT NULL,
    post_id BIGINT NOT NULL REFERENCES blog.posts(id) ON DELETE CASCADE,
    create_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--Индексы
CREATE INDEX idx_post_title ON blog.posts(title);
CREATE INDEX idx_post_created_at ON blog.posts(create_at DESC);
CREATE INDEX idx_comments_post_id ON blog.comments(post_id);
CREATE INDEX idx_post_tags_tag_id ON blog.post_tags(tag_id);
package ru.yandex.practicum.my_blog_back_app.persistence.old;

//@Component
//@RequiredArgsConstructor
public class TagRepositoryImpl implements ru.yandex.practicum.my_blog_back_app.persistence.old.repository.TagRepositoryOld {
//    private final JdbcClient jdbcClient;
//
//    @Override
//    public List<TagEntity> getTags(List<String> tags) {
//        return tags.stream()
//                .map(String::trim)
//                .map(tagName -> findTagByName(tagName)
//                        .orElseGet(() -> saveTag(tagName)))
//                .toList();
//    }
//
//    @Override
//    public void saveTagsAndPost(PostEntity postEntity) {
//        String tagSql = """
//                INSERT INTO blog.post_tags(post_id, tag_id)
//                VALUES (:postId, :tagId)
//                ON CONFLICT (post_id, tag_id) DO NOTHING;
//                """;
//
//
//        for (TagEntity tags : postEntity.getTags()) {
//            jdbcClient.sql(tagSql)
//                    .param("postId", postEntity.getId())
//                    .param("tagId", tags.getId())
//                    .update();
//        }
//    }
//
//    @Override
//    public List<TagEntity> findTagsByPostId(Long postId) {
//        String sql = """
//                SELECT t.id, t.name
//                FROM blog.tags t
//                JOIN blog.post_tags pt ON t.id = pt.tag_id
//                WHERE pt.post_id = :postId
//                ORDER BY t.name
//                """;
//
//        return jdbcClient.sql(sql)
//                .param("postId", postId)
//                .query(TagEntity.class)
//                .list();
//
//    }
//
//    @Override
//    public void deleteTagAndPost(Long postId) {
//        String sql = """
//                DELETE FROM blog.post_tags
//                WHERE post_id = :postId
//                """;
//
//        jdbcClient.sql(sql)
//                .param("postId", postId)
//                .update();
//    }
//
//    private TagEntity saveTag(String tag) {
//        String sql = """
//                INSERT INTO blog.tags(name)
//                VALUES (:name)
//                ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
//                RETURNING id, name;
//                """;
//
//        return jdbcClient.sql(sql)
//                .param("name", tag)
//                .query(TagEntity.class)
//                .single();
//    }
//
//    private Optional<TagEntity> findTagByName(String tag) {
//        String sql = """
//                SELECT * FROM blog.tags t
//                WHERE t.name = :name;
//                """;
//
//        return jdbcClient.sql(sql)
//                .param("name", tag)
//                .query(TagEntity.class)
//                .optional();
//    }
}

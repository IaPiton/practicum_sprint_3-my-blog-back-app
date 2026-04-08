package ru.yandex.practicum.my_blog_back_app.core.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса постов")
class PostServiceImplTest {
//
//    @Mock
//    private PostRepository postRepository;
//
//    @Mock
//    private TagRepository tagRepository;
//
//    @Mock
//    private CommentRepository commentRepository;
//
//    @Mock
//    private PostMapper postMapper;
//
//    @InjectMocks
//    private PostServiceImpl postService;
//
//    private PostEntity testPostEntity;
//    private PostResponse testPostResponse;
//    private PostPreview testPostPreview;
//    private List<TagEntity> testTags;
//
//    @BeforeEach
//    void setUp() {
//        testTags = List.of(
//                createTag(1L, "java"),
//                createTag(2L, "spring")
//        );
//
//        testPostEntity = createPostEntity("Содержание", testTags);
//
//        testPostResponse = PostResponse.builder()
//                .id(1L)
//                .title("Тестовый пост")
//                .text("Содержание")
//                .likesCount(5L)
//                .build();
//
//        testPostPreview = PostPreview.builder()
//                .id(1L)
//                .title("Тестовый пост")
//                .text("Очень длинное содержание поста, которое превышает сто двадцать восемь символов для проверки обрезки текста в превью.")
//                .likesCount(5L)
//                .build();
//    }
//
//    private TagEntity createTag(Long id, String name) {
//        TagEntity tag = new TagEntity();
//        tag.setId(id);
//        tag.setName(name);
//        return tag;
//    }
//
//    private PostEntity createPostEntity(String text, List<TagEntity> tags) {
//        PostEntity post = new PostEntity();
//        post.setId(1L);
//        post.setTitle("Тестовый пост");
//        post.setText(text);
//        post.setLikesCount(5L);
//        post.setImage(null);
//        post.setTags(tags);
//        return post;
//    }
//
//    @Nested
//    @DisplayName("Тесты получения списка постов")
//    class GetPostsTests {
//
//        @Test
//        @DisplayName("Должен вернуть список постов без поискового запроса")
//        void shouldReturnPostsWithoutSearch() {
//            List<PostEntity> posts = List.of(testPostEntity);
//            when(postRepository.findPostsWithFilters(eq(""), eq(List.of()), eq(10), eq(0)))
//                    .thenReturn(posts);
//            when(postRepository.countPostsWithFilters(eq(""), eq(List.of())))
//                    .thenReturn(1);
//            when(tagRepository.findTagsByPostId(1L)).thenReturn(testTags);
//            when(postMapper.toPreviewResponse(any(PostEntity.class), anyString()))
//                    .thenReturn(testPostPreview);
//
//            PostListResponse response = postService.getPosts(null, 1, 10);
//
//            assertThat(response).isNotNull();
//            assertThat(response.getPosts()).hasSize(1);
//            assertThat(response.getHasPrev()).isFalse();
//            assertThat(response.getHasNext()).isFalse();
//            assertThat(response.getLastPage()).isEqualTo(1);
//            verify(tagRepository).findTagsByPostId(1L);
//        }
//
//        @Test
//        @DisplayName("Должен корректно распарсить поиск по тегам и заголовку")
//        void shouldParseSearchWithTagsAndTitle() {
//            String searchQuery = "привет мир #java #spring";
//            List<PostEntity> posts = List.of(testPostEntity);
//            when(postRepository.findPostsWithFilters(eq("привет мир"), eq(List.of("java", "spring")), eq(10), eq(0)))
//                    .thenReturn(posts);
//            when(postRepository.countPostsWithFilters(eq("привет мир"), eq(List.of("java", "spring"))))
//                    .thenReturn(1);
//            when(tagRepository.findTagsByPostId(1L)).thenReturn(testTags);
//            when(postMapper.toPreviewResponse(any(), anyString())).thenReturn(testPostPreview);
//
//            PostListResponse response = postService.getPosts(searchQuery, 1, 10);
//
//            assertThat(response.getPosts()).hasSize(1);
//        }
//
//        @Test
//        @DisplayName("Должен корректно вычислить пагинацию для нескольких страниц")
//        void shouldCalculatePaginationCorrectly() {
//            List<PostEntity> posts = List.of(testPostEntity, testPostEntity);
//            when(postRepository.findPostsWithFilters(any(), any(), eq(2), eq(0)))
//                    .thenReturn(posts);
//            when(postRepository.countPostsWithFilters(any(), any()))
//                    .thenReturn(5);
//            when(tagRepository.findTagsByPostId(1L)).thenReturn(testTags);
//            when(postMapper.toPreviewResponse(any(), anyString())).thenReturn(testPostPreview);
//
//            PostListResponse response = postService.getPosts(null, 1, 2);
//
//            assertThat(response.getLastPage()).isEqualTo(3);
//            assertThat(response.getHasPrev()).isFalse();
//            assertThat(response.getHasNext()).isTrue();
//        }
//
//        @Test
//        @DisplayName("Должен вернуть пустой список если постов нет")
//        void shouldReturnEmptyListWhenNoPosts() {
//            when(postRepository.findPostsWithFilters(eq(""), eq(List.of()), eq(10), eq(0)))
//                    .thenReturn(List.of());
//            when(postRepository.countPostsWithFilters(eq(""), eq(List.of())))
//                    .thenReturn(0);
//
//            PostListResponse response = postService.getPosts(null, 1, 10);
//
//            assertThat(response.getPosts()).isEmpty();
//            assertThat(response.getLastPage()).isZero();
//        }
//    }
//
//    @Nested
//    @DisplayName("Тесты получения поста по ID")
//    class GetPostByIdTests {
//
//        @Test
//        @DisplayName("Должен вернуть пост, если он существует")
//        void shouldReturnPostWhenExists() {
//            when(postRepository.findById(1L)).thenReturn(Optional.of(testPostEntity));
//            when(tagRepository.findTagsByPostId(1L)).thenReturn(testTags);
//            when(postMapper.toResponse(testPostEntity)).thenReturn(testPostResponse);
//
//            PostResponse result = postService.getPostById(1L);
//
//            assertThat(result).isEqualTo(testPostResponse);
//            verify(tagRepository).findTagsByPostId(1L);
//        }
//
//        @Test
//        @DisplayName("Должен выбросить исключение, если пост не найден")
//        void shouldThrowExceptionWhenPostNotFound() {
//            when(postRepository.findById(99L)).thenReturn(Optional.empty());
//
//            assertThatThrownBy(() -> postService.getPostById(99L))
//                    .isInstanceOf(EntityNotFoundException.class);
//        }
//    }
//
//    @Nested
//    @DisplayName("Тесты создания поста")
//    class CreatePostTests {
//
//        @Test
//        @DisplayName("Должен успешно создать пост с тегами")
//        void shouldCreatePostWithTags() {
//            PostCreateRequest request = PostCreateRequest.builder()
//                    .title("Новый пост")
//                    .text("Содержание")
//                    .tags(List.of("java", "spring"))
//                    .build();
//
//            when(tagRepository.getTags(List.of("java", "spring"))).thenReturn(testTags);
//            when(postMapper.toEntity(request)).thenReturn(testPostEntity);
//            when(postRepository.savePost(testPostEntity)).thenReturn(testPostEntity);
//            when(postMapper.toResponse(testPostEntity)).thenReturn(testPostResponse);
//
//            PostResponse result = postService.createPost(request);
//
//            assertThat(result).isEqualTo(testPostResponse);
//            verify(tagRepository).getTags(List.of("java", "spring"));
//            verify(postRepository).savePost(testPostEntity);
//            verify(tagRepository).saveTagsAndPost(testPostEntity);
//        }
//
//        @Test
//        @DisplayName("Должен успешно создать пост без тегов")
//        void shouldCreatePostWithoutTags() {
//            PostCreateRequest request = PostCreateRequest.builder()
//                    .title("Новый пост")
//                    .text("Содержание")
//                    .tags(List.of())
//                    .build();
//
//            when(tagRepository.getTags(List.of())).thenReturn(List.of());
//            when(postMapper.toEntity(request)).thenReturn(testPostEntity);
//            when(postRepository.savePost(testPostEntity)).thenReturn(testPostEntity);
//            when(postMapper.toResponse(testPostEntity)).thenReturn(testPostResponse);
//
//            PostResponse result = postService.createPost(request);
//
//            assertThat(result).isEqualTo(testPostResponse);
//            verify(tagRepository).getTags(List.of());
//            verify(tagRepository, never()).saveTagsAndPost(any());
//        }
//    }
//
//    @Nested
//    @DisplayName("Тесты обновления поста")
//    class UpdatePostTests {
//
//        @Test
//        @DisplayName("Должен обновить пост без изменения тегов")
//        void shouldUpdatePostWithoutTags() {
//            PostUpdateRequest request = PostUpdateRequest.builder()
//                    .id(1L)
//                    .title("Обновленный заголовок")
//                    .text("Обновленный текст")
//                    .tags(List.of())
//                    .build();
//
//            when(postRepository.findById(1L)).thenReturn(Optional.of(testPostEntity));
//            when(tagRepository.findTagsByPostId(1L)).thenReturn(testTags);
//            when(postMapper.toResponse(any(PostEntity.class))).thenReturn(testPostResponse);
//
//            postService.updatePost(request);
//
//            assertThat(testPostEntity.getTitle()).isEqualTo("Обновленный заголовок");
//            assertThat(testPostEntity.getText()).isEqualTo("Обновленный текст");
//            verify(tagRepository, never()).deleteTagAndPost(anyLong());
//            verify(tagRepository, never()).saveTagsAndPost(any());
//            verify(postRepository).update(testPostEntity);
//        }
//
//        @Test
//        @DisplayName("Должен обновить пост с новыми тегами")
//        void shouldUpdatePostWithNewTags() {
//            List<TagEntity> newTags = List.of(createTag(3L, "newTag"));
//            PostUpdateRequest request = PostUpdateRequest.builder()
//                    .id(1L)
//                    .title("Обновленный заголовок")
//                    .text("Обновленный текст")
//                    .tags(List.of("newTag"))
//                    .build();
//
//            when(postRepository.findById(1L)).thenReturn(Optional.of(testPostEntity));
//            when(tagRepository.findTagsByPostId(1L)).thenReturn(testTags);
//            when(tagRepository.getTags(List.of("newTag"))).thenReturn(newTags);
//            when(postMapper.toResponse(any(PostEntity.class))).thenReturn(testPostResponse);
//
//            postService.updatePost(request);
//
//            verify(tagRepository).deleteTagAndPost(1L);
//            verify(tagRepository).getTags(List.of("newTag"));
//            verify(tagRepository).saveTagsAndPost(testPostEntity);
//            assertThat(testPostEntity.getTags()).isEqualTo(newTags);
//        }
//
//        @Test
//        @DisplayName("Должен выбросить исключение при обновлении несуществующего поста")
//        void shouldThrowExceptionWhenPostNotFoundOnUpdate() {
//            PostUpdateRequest request = PostUpdateRequest.builder()
//                    .id(99L)
//                    .title("Заголовок")
//                    .text("Текст")
//                    .tags(List.of())
//                    .build();
//
//            when(postRepository.findById(99L)).thenReturn(Optional.empty());
//
//            assertThatThrownBy(() -> postService.updatePost(request))
//                    .isInstanceOf(EntityNotFoundException.class);
//        }
//    }
//
//    @Nested
//    @DisplayName("Тесты удаления поста")
//    class DeletePostTests {
//
//        @Test
//        @DisplayName("Должен удалить пост и связанные сущности")
//        void shouldDeletePostAndRelatedEntities() {
//            Long postId = 1L;
//
//            postService.deletePost(postId);
//
//            verify(commentRepository).deleteByPostId(postId);
//            verify(tagRepository).deleteTagAndPost(postId);
//            verify(postRepository).delete(postId);
//        }
//    }
//
//    @Nested
//    @DisplayName("Тесты лайков")
//    class LikesTests {
//
//        @Test
//        @DisplayName("Должен увеличить количество лайков и вернуть новое значение")
//        void shouldIncrementLikesAndReturnNewCount() {
//            when(postRepository.findById(1L)).thenReturn(Optional.of(testPostEntity));
//            when(tagRepository.findTagsByPostId(1L)).thenReturn(testTags);
//
//            Long newLikesCount = postService.incrementLikes(1L);
//
//            assertThat(newLikesCount).isEqualTo(6L);
//            assertThat(testPostEntity.getLikesCount()).isEqualTo(6L);
//            verify(postRepository).update(testPostEntity);
//        }
//
//        @Test
//        @DisplayName("Должен выбросить исключение при лайке несуществующего поста")
//        void shouldThrowExceptionWhenPostNotFoundOnLike() {
//            when(postRepository.findById(99L)).thenReturn(Optional.empty());
//
//            assertThatThrownBy(() -> postService.incrementLikes(99L))
//                    .isInstanceOf(EntityNotFoundException.class);
//        }
//    }
//
//    @Nested
//    @DisplayName("Тесты работы с изображениями")
//    class ImageTests {
//
//        @Test
//        @DisplayName("Должен обновить изображение поста")
//        void shouldUpdatePostImage() {
//            byte[] imageData = new byte[]{1, 2, 3};
//            when(postRepository.findById(1L)).thenReturn(Optional.of(testPostEntity));
//            when(tagRepository.findTagsByPostId(1L)).thenReturn(testTags);
//
//            postService.updatePostImage(1L, imageData);
//
//            assertThat(testPostEntity.getImage()).isEqualTo(imageData);
//            verify(postRepository).update(testPostEntity);
//        }
//
//        @Test
//        @DisplayName("Должен выбросить исключение при обновлении изображения несуществующего поста")
//        void shouldThrowExceptionWhenPostNotFoundOnImageUpdate() {
//            byte[] imageData = new byte[]{1, 2, 3};
//            when(postRepository.findById(99L)).thenReturn(Optional.empty());
//
//            assertThatThrownBy(() -> postService.updatePostImage(99L, imageData))
//                    .isInstanceOf(EntityNotFoundException.class);
//        }
//
//        @Test
//        @DisplayName("Должен вернуть изображение поста")
//        void shouldGetPostImage() {
//            byte[] imageData = new byte[]{1, 2, 3};
//            testPostEntity.setImage(imageData);
//            when(postRepository.findById(1L)).thenReturn(Optional.of(testPostEntity));
//            when(tagRepository.findTagsByPostId(1L)).thenReturn(testTags);
//
//            byte[] result = postService.getPostImage(1L);
//
//            assertThat(result).isEqualTo(imageData);
//        }
//
//        @Test
//        @DisplayName("Должен выбросить исключение при получении изображения несуществующего поста")
//        void shouldThrowExceptionWhenPostNotFoundOnImageGet() {
//            when(postRepository.findById(99L)).thenReturn(Optional.empty());
//
//            assertThatThrownBy(() -> postService.getPostImage(99L))
//                    .isInstanceOf(EntityNotFoundException.class);
//        }
//    }
//
//    @Nested
//    @DisplayName("Тесты парсинга поискового запроса")
//    class ParseSearchStringTests {
//
//        @Test
//        @DisplayName("Должен корректно распарсить пустой поисковый запрос")
//        void shouldParseEmptySearch() {
//            postService.getPosts(null, 1, 10);
//
//            verify(postRepository).findPostsWithFilters(eq(""), eq(List.of()), anyInt(), anyInt());
//        }
//
//        @Test
//        @DisplayName("Должен корректно распарсить только теги")
//        void shouldParseOnlyTags() {
//            String searchQuery = "#java #spring";
//
//            postService.getPosts(searchQuery, 1, 10);
//
//            verify(postRepository).findPostsWithFilters(eq(""), eq(List.of("java", "spring")), anyInt(), anyInt());
//        }
//
//        @Test
//        @DisplayName("Должен корректно распарсить только текст")
//        void shouldParseOnlyText() {
//            String searchQuery = "привет мир";
//
//            postService.getPosts(searchQuery, 1, 10);
//
//            verify(postRepository).findPostsWithFilters(eq("привет мир"), eq(List.of()), anyInt(), anyInt());
//        }
//
//        @Test
//        @DisplayName("Должен обрезать длинный текст в превью до 128 символов с многоточием")
//        void shouldTruncateLongTextInPreview() {
//            String longText = "a".repeat(200);
//            PostEntity postWithLongText = createPostEntity(longText, testTags);
//            List<PostEntity> posts = List.of(postWithLongText);
//
//            when(postRepository.findPostsWithFilters(any(), any(), eq(10), eq(0)))
//                    .thenReturn(posts);
//            when(postRepository.countPostsWithFilters(any(), any()))
//                    .thenReturn(1);
//            when(tagRepository.findTagsByPostId(1L)).thenReturn(testTags);
//            when(postMapper.toPreviewResponse(eq(postWithLongText), anyString()))
//                    .thenAnswer(invocation -> {
//                        String truncated = invocation.getArgument(1);
//                        assertThat(truncated).hasSize(128 + 3);
//                        assertThat(truncated).endsWith("...");
//                        return testPostPreview;
//                    });
//
//            postService.getPosts(null, 1, 10);
//
//            verify(postMapper).toPreviewResponse(eq(postWithLongText), argThat(text -> text.length() == 131));
//        }
//
//        @Test
//        @DisplayName("Не должен обрезать короткий текст в превью")
//        void shouldNotTruncateShortTextInPreview() {
//            String shortText = "Короткий текст";
//            PostEntity postWithShortText = createPostEntity(shortText, testTags);
//            List<PostEntity> posts = List.of(postWithShortText);
//
//            when(postRepository.findPostsWithFilters(any(), any(), eq(10), eq(0)))
//                    .thenReturn(posts);
//            when(postRepository.countPostsWithFilters(any(), any()))
//                    .thenReturn(1);
//            when(tagRepository.findTagsByPostId(1L)).thenReturn(testTags);
//            when(postMapper.toPreviewResponse(eq(postWithShortText), eq(shortText)))
//                    .thenReturn(testPostPreview);
//
//            postService.getPosts(null, 1, 10);
//
//            verify(postMapper).toPreviewResponse(eq(postWithShortText), eq(shortText));
//        }
//    }
}
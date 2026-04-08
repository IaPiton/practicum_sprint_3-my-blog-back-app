package ru.yandex.practicum.my_blog_back_app.core.service;

//@Service
//@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
//    private final PostRepository postRepository;
//    private final TagRepository tagRepository;
//    private final CommentRepository commentRepository;
//    private final PostMapper postMapper;
//
//    @Override
//    public PostListResponse getPosts(String search, int pageNumber, int pageSize) {
//        SearchCriteria criteria = parseSearchString(search);
//
//        int offset = (pageNumber - 1) * pageSize;
//
//        List<PostEntity> posts = postRepository.findPostsWithFilters(
//                criteria.getTitleSubstring(),
//                criteria.getTags(),
//                pageSize,
//                offset
//        );
//
//        posts = posts
//                .stream()
//                .peek(post -> post.setTags(tagRepository.findTagsByPostId(post.getId())))
//                .toList();
//
//        int totalPosts = postRepository.countPostsWithFilters(
//                criteria.getTitleSubstring(),
//                criteria.getTags()
//        );
//
//        List<PostPreview> postDtos = posts.stream()
//                .map(this::convertToPreviewDto)
//                .toList();
//
//        int lastPage = (int) Math.ceil((double) totalPosts / pageSize);
//        boolean hasPrev = pageNumber > 1;
//        boolean hasNext = pageNumber < lastPage;
//
//        return PostListResponse.builder()
//                .posts(postDtos)
//                .hasPrev(hasPrev)
//                .hasNext(hasNext)
//                .lastPage(lastPage)
//                .build();
//    }
//
//    @Override
//    public PostResponse getPostById(Long postId) {
//        PostEntity postEntity = postRepository.findById(postId).orElseThrow(EntityNotFoundException::new);
//        postEntity.setTags(tagRepository.findTagsByPostId(postId));
//
//        return postMapper.toResponse(postEntity);
//    }
//
//    @Override
//    public PostResponse createPost(PostCreateRequest request) {
//        List<TagEntity> tags = tagRepository.getTags(request.getTags());
//        PostEntity postEntity = postMapper.toEntity(request);
//        postEntity = postRepository.savePost(postEntity);
//
//        postEntity.setTags(tags);
//
//        if (postEntity.getTags() != null && !postEntity.getTags().isEmpty()) {
//            tagRepository.saveTagsAndPost(postEntity);
//        }
//        return postMapper.toResponse(postEntity);
//    }
//
//    @Override
//    public PostResponse updatePost(PostUpdateRequest request) {
//        PostEntity postEntity = postRepository.findById(request.getId()).orElseThrow(EntityNotFoundException::new);
//        postEntity.setTags(tagRepository.findTagsByPostId(request.getId()));
//
//        postEntity.setTitle(request.getTitle());
//        postEntity.setText(request.getText());
//
//        if (!request.getTags().isEmpty()) {
//            tagRepository.deleteTagAndPost(request.getId());
//            List<TagEntity> tagEntities = tagRepository.getTags(request.getTags());
//            postEntity.setTags(tagEntities);
//            tagRepository.saveTagsAndPost(postEntity);
//        }
//
//        postRepository.update(postEntity);
//        return postMapper.toResponse(postEntity);
//    }
//
//    @Override
//    public void deletePost(Long postId) {
//        commentRepository.deleteByPostId(postId);
//        tagRepository.deleteTagAndPost(postId);
//        postRepository.delete(postId);
//    }
//
//    @Override
//    public Long incrementLikes(Long postId) {
//        PostEntity postEntity = postRepository.findById(postId).orElseThrow(EntityNotFoundException::new);
//        postEntity.setTags(tagRepository.findTagsByPostId(postId));
//
//        postEntity.setLikesCount(postEntity.getLikesCount() + 1);
//        postRepository.update(postEntity);
//        return postEntity.getLikesCount();
//    }
//
//    @Override
//    public void updatePostImage(Long postId, byte[] image) {
//        PostEntity postEntity = postRepository.findById(postId).orElseThrow(EntityNotFoundException::new);
//        postEntity.setTags(tagRepository.findTagsByPostId(postId));
//        postEntity.setImage(image);
//        postRepository.update(postEntity);
//    }
//
//    @Override
//    public byte[] getPostImage(Long postId) {
//        PostEntity postEntity = postRepository.findById(postId).orElseThrow(EntityNotFoundException::new);
//        postEntity.setTags(tagRepository.findTagsByPostId(postId));
//        return postEntity.getImage();
//    }
//
//
//    private SearchCriteria parseSearchString(String search) {
//        if (search == null || search.trim().isEmpty()) {
//            return new SearchCriteria("", Collections.emptyList());
//        }
//
//        String[] words = search.trim().split("\\s+");
//
//        List<String> tags = new ArrayList<>();
//        List<String> titleWords = new ArrayList<>();
//
//        for (String word : words) {
//            if (word.startsWith("#")) {
//                String tag = word.substring(1);
//                if (!tag.isEmpty()) {
//                    tags.add(tag);
//                }
//            } else if (!word.isEmpty()) {
//                titleWords.add(word);
//            }
//        }
//
//        String titleSubstring = String.join(" ", titleWords);
//
//        return new SearchCriteria(titleSubstring, tags);
//    }
//
//    private PostPreview convertToPreviewDto(PostEntity post) {
//        String truncatedText = post.getText();
//        if (truncatedText.length() > 128) {
//            truncatedText = truncatedText.substring(0, 128) + "...";
//        }
//
//        return postMapper.toPreviewResponse(post, truncatedText);
//    }

}
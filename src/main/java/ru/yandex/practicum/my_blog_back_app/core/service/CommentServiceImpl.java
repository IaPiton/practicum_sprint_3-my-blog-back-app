package ru.yandex.practicum.my_blog_back_app.core.service;

//@Service
//@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
//    private final CommentRepository commentRepository;
//    private final CommentMapper commentMapper;
//
//    @Override
//    public List<CommentResponse> getCommentsByPostId(Long postId) {
//        List<CommentsEntity> commentsEntities = commentRepository.findCommentsByPostId(postId);
//        return commentsEntities.stream()
//                .map(commentMapper::toResponse)
//                .toList();
//    }
//
//    @Override
//    public CommentResponse getCommentById(Long commentId) {
//        CommentsEntity commentsEntity = commentRepository.findById(commentId).orElseThrow(EntityNotFoundException::new);
//        return commentMapper.toResponse(commentsEntity);
//    }
//
//    @Override
//    public CommentResponse createComment(CommentCreateRequest request) {
//        CommentsEntity commentsEntity = new CommentsEntity();
//        commentsEntity.setPostId(request.getPostId());
//        commentsEntity.setText(request.getText());
//        commentsEntity.setId(commentRepository.save(commentsEntity));
//        return commentMapper.toResponse(commentsEntity);
//    }
//
//    @Override
//    public CommentResponse updateComment(Long commentId, CommentUpdateRequest request) {
//        CommentsEntity commentsEntity = commentRepository.findById(commentId).orElseThrow(EntityNotFoundException::new);
//        commentsEntity.setText(request.getText());
//        commentRepository.update(commentsEntity);
//        return commentMapper.toResponse(commentsEntity);
//    }
//
//    @Override
//    public void deleteComment(Long commentId) {
//        commentRepository.deleteByCommentId(commentId);
//    }
//
//    @Override
//    public boolean postExists(Long postId) {
//        return commentRepository.postExists(postId);
//    }

}
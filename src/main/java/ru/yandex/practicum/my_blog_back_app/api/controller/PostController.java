package ru.yandex.practicum.my_blog_back_app.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.PostCreateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.request.PostUpdateRequest;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostListResponse;
import ru.yandex.practicum.my_blog_back_app.api.dto.response.PostResponse;
import ru.yandex.practicum.my_blog_back_app.core.service.PostService;
import ru.yandex.practicum.my_blog_back_app.core.validator.ImageValidator;

import java.io.IOException;


@RestController
@RequestMapping("api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final ImageValidator imageValidator;

    @GetMapping
    public ResponseEntity<PostListResponse> getPosts(
            @RequestParam(value = "search", defaultValue = "") String search,
            @RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "5") int pageSize) {
        if (pageNumber < 0) {
            pageNumber = 0;
        }

        if (pageSize < 1) {
            pageSize = 5;
        }
        return ResponseEntity.ok(postService.getPosts(search, pageNumber, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @Valid @RequestBody PostUpdateRequest request) {

        return ResponseEntity.ok(postService.updatePost(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable("id") Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/likes")
    public ResponseEntity<Long> incrementLikes(@PathVariable("id") Long id) {
        return ResponseEntity.ok(postService.incrementLikes(id));
    }

    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updatePostImage(
            @PathVariable("id") Long postId,
            @RequestParam("image") MultipartFile image) {

        imageValidator.validate(image);

        try {
            postService.updatePostImage(postId, image.getBytes());
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении файла", e);
        }
    }

    @GetMapping(value = "/{id}/image",
            produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE,
                    MediaType.IMAGE_GIF_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public ResponseEntity<byte[]> getPostImage(@PathVariable("id") Long postId) {
        byte[] image = postService.getPostImage(postId);

        if (image == null || image.length == 0) {
            return ResponseEntity.ok().contentLength(0).body(null);
        }
        return ResponseEntity.ok()
                .contentLength(image.length)
                .body(image);
    }
}
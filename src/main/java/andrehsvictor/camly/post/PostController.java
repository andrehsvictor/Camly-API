package andrehsvictor.camly.post;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andrehsvictor.camly.post.dto.CreatePostDto;
import andrehsvictor.camly.post.dto.PostDto;
import andrehsvictor.camly.post.dto.UpdatePostDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/api/v1/posts")
    public ResponseEntity<PostDto> create(@Valid @RequestBody CreatePostDto createPostDto) {
        Post post = postService.create(createPostDto);
        PostDto postDto = postService.toDto(post);
        return ResponseEntity.status(201).body(postDto);
    }

    @PutMapping("/api/v1/posts/{id}/likes")
    public ResponseEntity<Void> like(@PathVariable UUID id) {
        postService.like(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/v1/posts/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/v1/posts/{id}")
    public ResponseEntity<PostDto> update(@PathVariable UUID id, @Valid @RequestBody UpdatePostDto updatePostDto) {
        Post post = postService.update(id, updatePostDto);
        PostDto postDto = postService.toDto(post);
        return ResponseEntity.ok(postDto);
    }

    @GetMapping("/api/v1/posts")
    public ResponseEntity<Page<PostDto>> getAll(
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) String username,
            Pageable pageable) {
        Page<Post> posts = postService.getAllWithFilters(query, username, pageable);
        Page<PostDto> postDtos = posts.map(postService::toDto);
        return ResponseEntity.ok(postDtos);
    }

    @GetMapping("/api/v1/posts/{id}")
    public ResponseEntity<PostDto> getById(@PathVariable UUID id) {
        Post post = postService.getById(id);
        PostDto postDto = postService.toDto(post);
        return ResponseEntity.ok(postDto);
    }

    @GetMapping("/api/v1/posts/stats")
    public ResponseEntity<PostStats> getPostStats() {
        PostStats postStats = postService.getPostStatsByCurrentUser();
        return ResponseEntity.ok(postStats);
    }

    @GetMapping("/api/v1/users/{userId}/posts")
    public ResponseEntity<Page<PostDto>> getAllByUserId(
            @PathVariable UUID userId,
            Pageable pageable) {
        Page<Post> posts = postService.getAllByUserId(userId, pageable);
        Page<PostDto> postDtos = posts.map(postService::toDto);
        return ResponseEntity.ok(postDtos);
    }

}

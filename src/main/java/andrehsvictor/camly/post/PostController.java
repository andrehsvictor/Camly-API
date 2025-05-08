package andrehsvictor.camly.post;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

}

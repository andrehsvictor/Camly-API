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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Post management operations")
@SecurityRequirement(name = "bearer-jwt")
public class PostController {

    private final PostService postService;

    @Operation(summary = "Create a new post", description = "Creates a new post with the provided caption and image URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid post data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token is required", content = @Content)
    })
    @PostMapping("/api/v1/posts")
    public ResponseEntity<PostDto> create(
            @Parameter(description = "Post creation data", required = true) @Valid @RequestBody CreatePostDto createPostDto) {
        Post post = postService.create(createPostDto);
        PostDto postDto = postService.toDto(post);
        return ResponseEntity.status(201).body(postDto);
    }

    @Operation(summary = "Toggle like status on a post", description = "Like or unlike a post by its ID. If the post is already liked, it will be unliked.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Like status toggled successfully"),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token is required", content = @Content)
    })
    @PutMapping("/api/v1/posts/{id}/likes")
    public ResponseEntity<Void> like(
            @Parameter(description = "Post ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id) {
        postService.like(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a post", description = "Deletes a post by its ID. Only the owner of the post can delete it.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Cannot delete a post owned by another user", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token is required", content = @Content)
    })
    @DeleteMapping("/api/v1/posts/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Post ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update a post", description = "Updates a post by its ID. Only the owner of the post can update it.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostDto.class))),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Cannot update a post owned by another user", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid post data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token is required", content = @Content)
    })
    @PutMapping("/api/v1/posts/{id}")
    public ResponseEntity<PostDto> update(
            @Parameter(description = "Post ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id,
            @Parameter(description = "Updated post data", required = true) @Valid @RequestBody UpdatePostDto updatePostDto) {
        Post post = postService.update(id, updatePostDto);
        PostDto postDto = postService.toDto(post);
        return ResponseEntity.ok(postDto);
    }

    @Operation(summary = "Get all posts with filtering", description = "Returns a paginated list of posts that can be filtered by caption or username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token is required", content = @Content)
    })
    @GetMapping("/api/v1/posts")
    public ResponseEntity<Page<PostDto>> getAll(
            @Parameter(description = "Search query for post caption") @RequestParam(required = false, name = "q") String query,
            @Parameter(description = "Filter by username") @RequestParam(required = false, name = "user.username") String username,
            @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable) {
        Page<Post> posts = postService.getAllWithFilters(query, username, pageable);
        Page<PostDto> postDtos = posts.map(postService::toDto);
        return ResponseEntity.ok(postDtos);
    }

    @Operation(summary = "Get post by ID", description = "Retrieves a specific post by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostDto.class))),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token is required", content = @Content)
    })
    @GetMapping("/api/v1/posts/{id}")
    public ResponseEntity<PostDto> getById(
            @Parameter(description = "Post ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id) {
        Post post = postService.getById(id);
        PostDto postDto = postService.toDto(post);
        return ResponseEntity.ok(postDto);
    }

    @Operation(summary = "Get post statistics", description = "Retrieves statistics about posts for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostStats.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token is required", content = @Content)
    })
    @GetMapping("/api/v1/posts/stats")
    public ResponseEntity<PostStats> getPostStats() {
        PostStats postStats = postService.getPostStatsByCurrentUser();
        return ResponseEntity.ok(postStats);
    }

    @Operation(summary = "Get posts by user ID", description = "Retrieves all posts for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token is required", content = @Content)
    })
    @GetMapping("/api/v1/users/{userId}/posts")
    public ResponseEntity<Page<PostDto>> getAllByUserId(
            @Parameter(description = "User ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID userId,
            @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable) {
        Page<Post> posts = postService.getAllByUserId(userId, pageable);
        Page<PostDto> postDtos = posts.map(postService::toDto);
        return ResponseEntity.ok(postDtos);
    }
}
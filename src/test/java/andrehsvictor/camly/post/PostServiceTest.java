package andrehsvictor.camly.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import andrehsvictor.camly.exception.ForbiddenOperationException;
import andrehsvictor.camly.exception.ResourceNotFoundException;
import andrehsvictor.camly.jwt.JwtService;
import andrehsvictor.camly.post.dto.CreatePostDto;
import andrehsvictor.camly.post.dto.PostDto;
import andrehsvictor.camly.post.dto.UpdatePostDto;
import andrehsvictor.camly.user.User;
import andrehsvictor.camly.user.UserService;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapper postMapper;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @InjectMocks
    private PostService postService;

    private UUID currentUserId;
    private UUID postId;
    private UUID otherUserId;
    private User currentUser;
    private User otherUser;
    private Post testPost;
    private PostDto testPostDto;
    private CreatePostDto createPostDto;
    private UpdatePostDto updatePostDto;
    private LocalDateTime now;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        postId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        now = LocalDateTime.now();
        pageable = PageRequest.of(0, 10);

        // Setup users
        currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setUsername("testuser");
        currentUser.setFollowerCount(10);

        otherUser = new User();
        otherUser.setId(otherUserId);
        otherUser.setUsername("otheruser");

        // Setup post
        testPost = new Post();
        testPost.setId(postId);
        testPost.setCaption("Test Caption");
        testPost.setImageUrl("https://example.com/image.jpg");
        testPost.setCreatedAt(now);
        testPost.setUpdatedAt(now);
        testPost.setUser(currentUser);
        testPost.setUser(currentUser);
        testPost.setLikeCount(5);
        testPost.setEngagementRate(0.5f);
        testPost.setLikes(new HashSet<>());

        // Setup DTOs
        createPostDto = CreatePostDto.builder()
                .caption("Test Caption")
                .imageUrl("https://example.com/image.jpg")
                .build();

        updatePostDto = UpdatePostDto.builder()
                .caption("Updated Caption")
                .build();

        testPostDto = new PostDto();
        testPostDto.setId(postId.toString());
        testPostDto.setCaption("Test Caption");
        testPostDto.setImageUrl("https://example.com/image.jpg");
    }

    @Test
    @DisplayName("Should get post by id successfully")
    void shouldGetPostByIdSuccessfully() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));

        Post result = postService.getById(postId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(postId);
        assertThat(result.getCaption()).isEqualTo("Test Caption");
        verify(postRepository, times(1)).findById(postId);
    }

    @Test
    @DisplayName("Should throw exception when post not found")
    void shouldThrowExceptionWhenPostNotFound() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getById(postId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Post not found with ID: '" + postId + "'");
    }

    @Test
    @DisplayName("Should get all posts with filters")
    void shouldGetAllPostsWithFilters() {
        String query = "test";
        String username = "testuser";
        Page<Post> expectedPage = new PageImpl<>(Collections.singletonList(testPost));

        when(postRepository.findAllWithFilters(query, username, pageable)).thenReturn(expectedPage);

        Page<Post> result = postService.getAllWithFilters(query, username, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(postId);
        verify(postRepository, times(1)).findAllWithFilters(query, username, pageable);
    }

    @Test
    @DisplayName("Should get posts by engagement rate")
    void shouldGetPostsByEngagementRate() {
        LocalDateTime startDate = now.minusDays(7);
        LocalDateTime endDate = now;
        Page<Post> expectedPage = new PageImpl<>(Collections.singletonList(testPost));

        when(postRepository.findAllByCreatedAtBetweenOrderByEngagementRateDesc(startDate, endDate, pageable))
                .thenReturn(expectedPage);

        Page<Post> result = postService.getAllByEngagementRate(startDate, endDate, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(postId);
        verify(postRepository, times(1)).findAllByCreatedAtBetweenOrderByEngagementRateDesc(startDate, endDate,
                pageable);
    }

    @Test
    @DisplayName("Should get all posts by user id")
    void shouldGetAllPostsByUserId() {
        Page<Post> expectedPage = new PageImpl<>(Collections.singletonList(testPost));

        when(postRepository.findAllByUserId(currentUserId, pageable)).thenReturn(expectedPage);

        Page<Post> result = postService.getAllByUserId(currentUserId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(postId);
        verify(postRepository, times(1)).findAllByUserId(currentUserId, pageable);
    }

    @Test
    @DisplayName("Should get post stats by current user")
    void shouldGetPostStatsByCurrentUser() {
        PostStats expectedStats = mock(PostStats.class);
        when(expectedStats.getTotalPosts()).thenReturn(10L);
        when(expectedStats.getTotalLikes()).thenReturn(50L);

        when(jwtService.getCurrentUserId()).thenReturn(currentUserId);
        when(postRepository.getPostStatsByUserId(currentUserId)).thenReturn(expectedStats);

        PostStats result = postService.getPostStatsByCurrentUser();

        assertThat(result).isNotNull();
        assertThat(result.getTotalPosts()).isEqualTo(10);
        assertThat(result.getTotalLikes()).isEqualTo(50);
        verify(postRepository, times(1)).getPostStatsByUserId(currentUserId);
    }

    @Test
    @DisplayName("Should check if post is liked by current user")
    void shouldCheckIfPostIsLikedByCurrentUser() {
        when(jwtService.getCurrentUserId()).thenReturn(currentUserId);
        when(postRepository.existsLikeByUserIdAndPostId(currentUserId, postId)).thenReturn(true);

        boolean result = postService.isLiked(postId);

        assertThat(result).isTrue();
        verify(postRepository, times(1)).existsLikeByUserIdAndPostId(currentUserId, postId);
    }

    @Test
    @DisplayName("Should like a post when not already liked")
    void shouldLikePostWhenNotAlreadyLiked() {
        when(jwtService.getCurrentUserId()).thenReturn(currentUserId);
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(postRepository.existsLikeByUserIdAndPostId(currentUserId, postId)).thenReturn(false);
        when(userService.getById(currentUserId)).thenReturn(currentUser);

        postService.like(postId);

        verify(postRepository, times(1)).save(testPost);
        assertThat(testPost.getLikeCount()).isEqualTo(6); // Initial count 5 + 1
    }

    @Test
    @DisplayName("Should unlike a post when already liked")
    void shouldUnlikePostWhenAlreadyLiked() {
        // Add the current user to the likes set
        testPost.getLikes().add(currentUser);

        when(jwtService.getCurrentUserId()).thenReturn(currentUserId);
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(postRepository.existsLikeByUserIdAndPostId(currentUserId, postId)).thenReturn(true);

        postService.like(postId);

        verify(postRepository, times(1)).save(testPost);
        assertThat(testPost.getLikeCount()).isEqualTo(4); // Initial count 5 - 1
    }

    @Test
    @DisplayName("Should create post successfully")
    void shouldCreatePostSuccessfully() {
        Post newPost = new Post();
        newPost.setCaption(createPostDto.getCaption());
        newPost.setImageUrl(createPostDto.getImageUrl());

        when(jwtService.getCurrentUserId()).thenReturn(currentUserId);
        when(postMapper.createPostDtoToPost(createPostDto)).thenReturn(newPost);
        when(userService.getById(currentUserId)).thenReturn(currentUser);
        when(postRepository.save(any(Post.class))).thenReturn(newPost);

        Post result = postService.create(createPostDto);

        assertThat(result).isNotNull();
        assertThat(result.getCaption()).isEqualTo(createPostDto.getCaption());
        assertThat(result.getImageUrl()).isEqualTo(createPostDto.getImageUrl());
        assertThat(result.getUser()).isEqualTo(currentUser);
        verify(postRepository, times(1)).save(newPost);
    }

    @Test
    @DisplayName("Should update post successfully")
    void shouldUpdatePostSuccessfully() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(jwtService.getCurrentUserId()).thenReturn(currentUserId);
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        Post result = postService.update(postId, updatePostDto);

        assertThat(result).isNotNull();
        verify(postMapper, times(1)).updatePostFromUpdatePostDto(updatePostDto, testPost);
        verify(postRepository, times(1)).save(testPost);
    }

    @Test
    @DisplayName("Should throw exception when updating post not owned by user")
    void shouldThrowExceptionWhenUpdatingPostNotOwnedByUser() {
        // Set a different user ID as the current user
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(jwtService.getCurrentUserId()).thenReturn(otherUserId);

        assertThatThrownBy(() -> postService.update(postId, updatePostDto))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("You are not the owner of this post");

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("Should delete post successfully")
    void shouldDeletePostSuccessfully() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(jwtService.getCurrentUserId()).thenReturn(currentUserId);

        postService.delete(postId);

        verify(postRepository, times(1)).delete(testPost);
    }

    @Test
    @DisplayName("Should throw exception when deleting post not owned by user")
    void shouldThrowExceptionWhenDeletingPostNotOwnedByUser() {
        // Set a different user ID as the current user
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(jwtService.getCurrentUserId()).thenReturn(otherUserId);

        assertThatThrownBy(() -> postService.delete(postId))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("You are not the owner of this post");

        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("Should convert post to DTO")
    void shouldConvertPostToDto() {
        when(postMapper.postToPostDto(testPost)).thenReturn(testPostDto);

        PostDto result = postService.toDto(testPost);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testPost.getId().toString());
        assertThat(result.getCaption()).isEqualTo(testPost.getCaption());
        verify(postMapper, times(1)).postToPostDto(testPost);
    }

    @Test
    @DisplayName("Should calculate engagement rate correctly")
    void shouldCalculateEngagementRateCorrectly() {
        // This test indirectly tests the calculateEngagementRate method through the
        // like method
        testPost.setLikeCount(5);
        currentUser.setFollowerCount(10);
        testPost.setUser(currentUser);

        when(jwtService.getCurrentUserId()).thenReturn(currentUserId);
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(postRepository.existsLikeByUserIdAndPostId(currentUserId, postId)).thenReturn(false);
        when(userService.getById(currentUserId)).thenReturn(currentUser);

        postService.like(postId);

        // The new engagement rate should be (5+1)/10 = 0.6
        assertThat(testPost.getEngagementRate()).isEqualTo(0.6f);
    }

    @Test
    @DisplayName("Should handle engagement rate when follower count is zero")
    void shouldHandleEngagementRateWhenFollowerCountIsZero() {
        // Set follower count to 0
        testPost.setLikeCount(5);
        currentUser.setFollowerCount(0);
        testPost.setUser(currentUser);

        when(jwtService.getCurrentUserId()).thenReturn(currentUserId);
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(postRepository.existsLikeByUserIdAndPostId(currentUserId, postId)).thenReturn(false);
        when(userService.getById(currentUserId)).thenReturn(currentUser);

        postService.like(postId);

        // When follower count is 0, engagement rate should equal like count
        assertThat(testPost.getEngagementRate()).isEqualTo(6.0f);
    }
}
package andrehsvictor.camly.post;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andrehsvictor.camly.exception.ForbiddenOperationException;
import andrehsvictor.camly.exception.ResourceNotFoundException;
import andrehsvictor.camly.jwt.JwtService;
import andrehsvictor.camly.post.dto.CreatePostDto;
import andrehsvictor.camly.post.dto.PostDto;
import andrehsvictor.camly.post.dto.UpdatePostDto;
import andrehsvictor.camly.user.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final JwtService jwtService;
    private final UserService userService;

    public Page<Post> getAllWithFilters(String query, String username, Pageable pageable) {
        return postRepository.findAllWithFilters(query, username, pageable);
    }

    public Page<Post> getAllByEngagementRate(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return postRepository.findAllByOrderByEngagementRateDescAndCreatedAtBetween(startDate, endDate, pageable);
    }

    public Page<Post> getAllByUserId(UUID userId, Pageable pageable) {
        return postRepository.findAllByUserId(userId, pageable);
    }

    public Post getById(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "ID", id));
    }

    public PostDto toDto(Post post) {
        return postMapper.postToPostDto(post);
    }

    public PostStats getPostStatsByCurrentUser() {
        return postRepository.getPostStatsByUserId(jwtService.getCurrentUserId());
    }

    public boolean isLiked(UUID postId) {
        return postRepository.isLikedByUser(postId, jwtService.getCurrentUserId());
    }

    @Transactional
    public boolean like(UUID postId) {
        UUID userId = jwtService.getCurrentUserId();
        Post post = getById(postId);

        if (postRepository.isLikedByUser(postId, userId)) {
            post.decrementLikeCount();
            return postRepository.unlike(postId, userId);
        } else {
            post.incrementLikeCount();
            return postRepository.like(postId, userId);
        }
    }

    @Transactional
    public Post create(CreatePostDto createPostDto) {
        Post post = postMapper.createPostDtoToPost(createPostDto);
        post.setUser(userService.getById(jwtService.getCurrentUserId()));
        return postRepository.save(post);
    }

    @Transactional
    public Post update(UUID id, UpdatePostDto updatePostDto) {
        Post post = getById(id);
        validatePostOwnership(post);
        postMapper.updatePostFromUpdatePostDto(updatePostDto, post);
        return postRepository.save(post);
    }

    @Transactional
    public void delete(UUID id) {
        Post post = getById(id);
        validatePostOwnership(post);
        postRepository.delete(post);
    }

    private void validatePostOwnership(Post post) {
        if (!post.getUserId().equals(jwtService.getCurrentUserId())) {
            throw new ForbiddenOperationException("You are not the owner of this post");
        }
    }
}
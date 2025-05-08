package andrehsvictor.camly.post;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
@CacheConfig(cacheNames = "posts")
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final JwtService jwtService;
    private final UserService userService;

    @Cacheable(key = "'post_' + #id")
    public Post getById(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "ID", id));
    }

    @Cacheable(key = "'filters_' + #query + '_' + #username + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Post> getAllWithFilters(String query, String username, Pageable pageable) {
        return postRepository.findAllWithFilters(query, username, pageable);
    }

    @Cacheable(key = "'engagement_' + #startDate + '_' + #endDate + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Post> getAllByEngagementRate(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return postRepository.findAllByOrderByEngagementRateDescAndCreatedAtBetween(startDate, endDate, pageable);
    }

    @Cacheable(key = "'userPosts_' + #userId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Post> getAllByUserId(UUID userId, Pageable pageable) {
        return postRepository.findAllByUserId(userId, pageable);
    }

    @Cacheable(key = "'stats_' + @jwtService.getCurrentUserId()")
    public PostStats getPostStatsByCurrentUser() {
        return postRepository.getPostStatsByUserId(jwtService.getCurrentUserId());
    }

    @Cacheable(key = "'liked_' + #postId + '_' + @jwtService.getCurrentUserId()")
    public boolean isLiked(UUID postId) {
        return postRepository.existsLikeByUserIdAndPostId(jwtService.getCurrentUserId(), postId);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(key = "'liked_' + #postId + '_' + @jwtService.getCurrentUserId()"),
            @CacheEvict(key = "'post_' + #postId"),
            @CacheEvict(key = "'stats_' + @jwtService.getCurrentUserId()")
    })
    public void like(UUID postId) {
        UUID userId = jwtService.getCurrentUserId();
        Post post = getById(postId);
        boolean isLiked = postRepository.existsLikeByUserIdAndPostId(userId, postId);

        if (isLiked) {
            post.getLikes().removeIf(like -> like.getId().equals(userId));
            post.decrementLikeCount();
        } else {
            post.getLikes().add(userService.getById(userId));
            post.incrementLikeCount();
        }

        calculateEngagementRate(post);
        postRepository.save(post);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(key = "'userPosts_*'", allEntries = true),
            @CacheEvict(key = "'stats_' + @jwtService.getCurrentUserId()"),
            @CacheEvict(key = "'filters_*'", allEntries = true),
            @CacheEvict(key = "'engagement_*'", allEntries = true)
    })
    public Post create(CreatePostDto createPostDto) {
        Post post = postMapper.createPostDtoToPost(createPostDto);
        post.setUser(userService.getById(jwtService.getCurrentUserId()));

        return postRepository.save(post);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(key = "'post_' + #id"),
            @CacheEvict(key = "'userPosts_*'", allEntries = true),
            @CacheEvict(key = "'filters_*'", allEntries = true),
            @CacheEvict(key = "'engagement_*'", allEntries = true)
    })
    public Post update(UUID id, UpdatePostDto updatePostDto) {
        Post post = getById(id);
        validateOwnership(post);

        postMapper.updatePostFromUpdatePostDto(updatePostDto, post);
        return postRepository.save(post);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(key = "'post_' + #id"),
            @CacheEvict(key = "'userPosts_*'", allEntries = true),
            @CacheEvict(key = "'stats_*'", allEntries = true),
            @CacheEvict(key = "'filters_*'", allEntries = true),
            @CacheEvict(key = "'engagement_*'", allEntries = true),
            @CacheEvict(key = "'liked_*'", allEntries = true)
    })
    public void delete(UUID id) {
        Post post = getById(id);
        validateOwnership(post);

        postRepository.delete(post);
    }

    public PostDto toDto(Post post) {
        return postMapper.postToPostDto(post);
    }

    private void calculateEngagementRate(Post post) {
        int followerCount = post.getUser().getFollowerCount();
        float engagementRate = followerCount > 0
                ? (float) post.getLikeCount() / followerCount
                : post.getLikeCount();

        post.setEngagementRate(engagementRate);
    }

    private void validateOwnership(Post post) {
        if (!post.getUserId().equals(jwtService.getCurrentUserId())) {
            throw new ForbiddenOperationException("You are not the owner of this post");
        }
    }
}
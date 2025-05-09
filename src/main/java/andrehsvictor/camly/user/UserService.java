package andrehsvictor.camly.user;

import java.util.List;
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
import andrehsvictor.camly.user.dto.UserDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "users")
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    @Cacheable(key = "'userById_' + #id")
    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", id));
    }

    @Cacheable(key = "'userByEmail_' + #email")
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    @Cacheable(key = "'userByProviderId_' + #providerId")
    public User getByProviderId(String providerId) {
        return userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "provider ID", providerId));
    }

    @Cacheable(key = "'filters_' + #query + '_' + #username + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<User> getAllWithFilters(String query, String username, Pageable pageable) {
        return userRepository.findAllWithFilters(query, username, pageable);
    }

    @Cacheable(key = "'existsByUsername_' + #username")
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Cacheable(key = "'existsByEmail_' + #email")
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Cacheable(key = "'userByToken_' + #token")
    public User getByToken(String token, TokenType type) {
        return switch (type) {
            case EMAIL_VERIFICATION -> userRepository.findByEmailVerificationToken(token)
                    .orElseThrow(() -> new ResourceNotFoundException("Email verification token not found"));
            case PASSWORD_RESET -> userRepository.findByResetPasswordToken(token)
                    .orElseThrow(() -> new ResourceNotFoundException("Password reset token not found"));
        };
    }

    public UserDto toDto(User user) {
        return userMapper.userToUserDto(user);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(key = "'userById_' + #user.id", condition = "#user.id != null"),
            @CacheEvict(key = "'userByEmail_' + #user.email", condition = "#user.email != null"),
            @CacheEvict(key = "'userByProviderId_' + #user.providerId", condition = "#user.providerId != null"),
            @CacheEvict(key = "'existsByUsername_' + #user.username", condition = "#user.username != null"),
            @CacheEvict(key = "'existsByEmail_' + #user.email", condition = "#user.email != null")
    })
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(key = "'userById_' + #id"),
            @CacheEvict(key = "'filters_*'", allEntries = true),
            @CacheEvict(cacheNames = "accounts", allEntries = true)
    })
    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(key = "'userById_' + #followedId"),
            @CacheEvict(key = "'userById_' + @jwtService.getCurrentUserId()"),
            @CacheEvict(key = "'filters_*'", allEntries = true)
    })
    public boolean follow(UUID followedId) {
        UUID followerId = jwtService.getCurrentUserId();

        if (followerId.equals(followedId)) {
            throw new ForbiddenOperationException("You cannot follow yourself");
        }

        User followed = getById(followedId);
        User follower = getById(followerId);

        boolean isAlreadyFollowing = followed.getFollowers().contains(follower);
        if (isAlreadyFollowing) {
            followed.getFollowers().remove(follower);
            updateFollowCounts(followed, follower, -1);
        } else {
            followed.getFollowers().add(follower);
            updateFollowCounts(followed, follower, 1);
        }

        userRepository.saveAll(List.of(follower, followed));
        return !isAlreadyFollowing;
    }

    private void updateFollowCounts(User followed, User follower, int delta) {
        if (delta > 0) {
            followed.incrementFollowerCount();
            follower.incrementFollowingCount();
        } else {
            followed.decrementFollowerCount();
            follower.decrementFollowingCount();
        }
    }

}
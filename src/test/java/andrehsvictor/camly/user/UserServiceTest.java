package andrehsvictor.camly.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import andrehsvictor.camly.user.dto.UserDto;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User testUser;
    private User secondUser;
    private UUID userId;
    private UUID secondUserId;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        secondUserId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .password("encodedPassword")
                .emailVerified(true)
                .bio("Test bio")
                .pictureUrl("https://example.com/picture.jpg")
                .followerCount(0)
                .followingCount(0)
                .postCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        secondUser = User.builder()
                .id(secondUserId)
                .username("seconduser")
                .email("second@example.com")
                .fullName("Second User")
                .password("encodedPassword")
                .emailVerified(true)
                .bio("Second user bio")
                .followerCount(0)
                .followingCount(0)
                .postCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userDto = UserDto.builder()
                .id(userId.toString())
                .username("testuser")
                .fullName("Test User")
                .bio("Test bio")
                .pictureUrl("https://example.com/picture.jpg")
                .followerCount(0)
                .followingCount(0)
                .postCount(0)
                .createdAt(testUser.getCreatedAt().toString())
                .build();
    }

    @Test
    @DisplayName("Should get user by ID")
    void shouldGetUserById() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        User result = userService.getById(userId);

        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw exception when user ID not found")
    void shouldThrowExceptionWhenUserIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("ID");
    }

    @Test
    @DisplayName("Should get user by email")
    void shouldGetUserByEmail() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        User result = userService.getByEmail(email);

        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should throw exception when email not found")
    void shouldThrowExceptionWhenEmailNotFound() {
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByEmail(nonExistentEmail))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("email");
    }

    @Test
    @DisplayName("Should get user by provider ID")
    void shouldGetUserByProviderId() {
        String providerId = "google123";
        when(userRepository.findByProviderId(providerId)).thenReturn(Optional.of(testUser));

        User result = userService.getByProviderId(providerId);

        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByProviderId(providerId);
    }

    @Test
    @DisplayName("Should throw exception when provider ID not found")
    void shouldThrowExceptionWhenProviderIdNotFound() {
        String nonExistentProviderId = "nonexistent123";
        when(userRepository.findByProviderId(nonExistentProviderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByProviderId(nonExistentProviderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("provider ID");
    }

    @Test
    @DisplayName("Should get all users with filters")
    void shouldGetAllUsersWithFilters() {
        String query = "test";
        String username = null;
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> expectedPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findAllWithFilters(query, username, pageable)).thenReturn(expectedPage);

        Page<User> result = userService.getAllWithFilters(query, username, pageable);

        assertThat(result).isEqualTo(expectedPage);
        assertThat(result.getContent()).containsExactly(testUser);
        verify(userRepository).findAllWithFilters(query, username, pageable);
    }

    @Test
    @DisplayName("Should check if username exists")
    void shouldCheckIfUsernameExists() {
        String username = "testuser";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        boolean result = userService.existsByUsername(username);

        assertThat(result).isTrue();
        verify(userRepository).existsByUsername(username);
    }

    @Test
    @DisplayName("Should check if email exists")
    void shouldCheckIfEmailExists() {
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        boolean result = userService.existsByEmail(email);

        assertThat(result).isTrue();
        verify(userRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("Should save user")
    void shouldSaveUser() {
        when(userRepository.save(testUser)).thenReturn(testUser);

        User result = userService.save(testUser);

        assertThat(result).isEqualTo(testUser);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should delete user by ID")
    void shouldDeleteUserById() {
        userService.deleteById(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("Should follow user successfully")
    void shouldFollowUserSuccessfully() {
        when(jwtService.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(secondUserId)).thenReturn(Optional.of(secondUser));

        boolean result = userService.follow(secondUserId);

        assertThat(result).isTrue();
        verify(userRepository).saveAll(any());

        assertThat(secondUser.getFollowerCount()).isEqualTo(1);
        assertThat(testUser.getFollowingCount()).isEqualTo(1);
        assertThat(secondUser.getFollowers()).contains(testUser);
    }

    @Test
    @DisplayName("Should unfollow user when following twice")
    void shouldUnfollowUserWhenFollowingTwice() {
        when(jwtService.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(secondUserId)).thenReturn(Optional.of(secondUser));

        secondUser.getFollowers().add(testUser);
        secondUser.setFollowerCount(1);
        testUser.setFollowingCount(1);

        boolean result = userService.follow(secondUserId);

        assertThat(result).isFalse();
        verify(userRepository).saveAll(any());

        assertThat(secondUser.getFollowerCount()).isEqualTo(0);
        assertThat(testUser.getFollowingCount()).isEqualTo(0);
        assertThat(secondUser.getFollowers()).doesNotContain(testUser);
    }

    @Test
    @DisplayName("Should throw exception when trying to follow self")
    void shouldThrowExceptionWhenTryingToFollowSelf() {
        when(jwtService.getCurrentUserId()).thenReturn(userId);

        assertThatThrownBy(() -> userService.follow(userId))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("cannot follow yourself");

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should get user by token")
    void shouldGetUserByToken() {
        String token = "valid-token";
        TokenType tokenType = TokenType.EMAIL_VERIFICATION;

        when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.of(testUser));

        User result = userService.getByToken(token, tokenType);

        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByEmailVerificationToken(token);
    }

    @Test
    @DisplayName("Should throw exception when token not found")
    void shouldThrowExceptionWhenTokenNotFound() {
        String token = "invalid-token";
        TokenType tokenType = TokenType.EMAIL_VERIFICATION;

        when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByToken(token, tokenType))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Email verification token not found");
    }

    @Test
    @DisplayName("Should convert user to DTO")
    void shouldConvertUserToDto() {
        when(userMapper.userToUserDto(testUser)).thenReturn(userDto);

        UserDto result = userService.toDto(testUser);

        assertThat(result).isEqualTo(userDto);
        verify(userMapper).userToUserDto(testUser);
    }
}
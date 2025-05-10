package andrehsvictor.camly.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import andrehsvictor.camly.account.dto.AccountDto;
import andrehsvictor.camly.account.dto.CreateAccountDto;
import andrehsvictor.camly.account.dto.PasswordResetDto;
import andrehsvictor.camly.account.dto.SendActionEmailDto;
import andrehsvictor.camly.account.dto.UpdateAccountDto;
import andrehsvictor.camly.account.dto.UpdatePasswordDto;
import andrehsvictor.camly.account.dto.VerifyEmailDto;
import andrehsvictor.camly.exception.BadRequestException;
import andrehsvictor.camly.exception.ResourceConflictException;
import andrehsvictor.camly.jwt.JwtService;
import andrehsvictor.camly.user.User;
import andrehsvictor.camly.user.UserProvider;
import andrehsvictor.camly.user.UserService;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailVerifier emailVerifier;

    @Mock
    private PasswordResetter passwordResetter;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .password("hashed_password")
                .provider(UserProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .emailVerified(true)
                .build();

        lenient().when(jwtService.getCurrentUserId()).thenReturn(userId);
        lenient().when(userService.getById(userId)).thenReturn(testUser);
    }

    @Test
    @DisplayName("Should get current account")
    void shouldGetCurrentAccount() {
        AccountDto expectedDto = new AccountDto();
        when(accountMapper.userToAccountDto(testUser)).thenReturn(expectedDto);

        AccountDto result = accountService.get();

        assertEquals(expectedDto, result);
        verify(userService).getById(userId);
        verify(accountMapper).userToAccountDto(testUser);
    }

    @Test
    @DisplayName("Should create account successfully")
    void shouldCreateAccountSuccessfully() {
        CreateAccountDto createDto = CreateAccountDto.builder()
                .username("newuser")
                .email("new@example.com")
                .fullName("New User")
                .password("password123")
                .build();

        User newUser = User.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password123")
                .build();

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username("newuser")
                .email("new@example.com")
                .password("encoded_password")
                .build();

        AccountDto expectedDto = new AccountDto();

        when(userService.existsByUsername(createDto.getUsername())).thenReturn(false);
        when(userService.existsByEmail(createDto.getEmail())).thenReturn(false);
        when(accountMapper.createAccountDtoToUser(createDto)).thenReturn(newUser);
        when(passwordEncoder.encode(newUser.getPassword())).thenReturn("encoded_password");
        when(userService.save(newUser)).thenReturn(savedUser);
        when(accountMapper.userToAccountDto(savedUser)).thenReturn(expectedDto);

        AccountDto result = accountService.create(createDto);

        assertEquals(expectedDto, result);
        verify(userService).existsByUsername(createDto.getUsername());
        verify(userService).existsByEmail(createDto.getEmail());
        verify(accountMapper).createAccountDtoToUser(createDto);
        verify(passwordEncoder).encode(createDto.getPassword());
        verify(userService).save(newUser);
        verify(accountMapper).userToAccountDto(savedUser);
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        CreateAccountDto createDto = CreateAccountDto.builder()
                .username("existinguser")
                .email("new@example.com")
                .fullName("New User")
                .password("password123")
                .build();

        when(userService.existsByUsername(createDto.getUsername())).thenReturn(true);

        ResourceConflictException exception = assertThrows(ResourceConflictException.class,
                () -> accountService.create(createDto));

        assertEquals("Username already taken", exception.getMessage());
        verify(userService).existsByUsername(createDto.getUsername());
        verify(userService, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        CreateAccountDto createDto = CreateAccountDto.builder()
                .username("newuser")
                .email("existing@example.com")
                .fullName("New User")
                .password("password123")
                .build();

        when(userService.existsByUsername(createDto.getUsername())).thenReturn(false);
        when(userService.existsByEmail(createDto.getEmail())).thenReturn(true);

        ResourceConflictException exception = assertThrows(ResourceConflictException.class,
                () -> accountService.create(createDto));

        assertEquals("Email already taken", exception.getMessage());
        verify(userService).existsByUsername(createDto.getUsername());
        verify(userService).existsByEmail(createDto.getEmail());
        verify(userService, never()).save(any());
    }

    @Test
    @DisplayName("Should update account successfully")
    void shouldUpdateAccountSuccessfully() {
        UpdateAccountDto updateDto = UpdateAccountDto.builder()
                .username("testuser")
                .email("test@example.com")
                .fullName("Updated Name")
                .bio("New bio")
                .build();

        AccountDto expectedDto = new AccountDto();

        when(accountMapper.userToAccountDto(testUser)).thenReturn(expectedDto);

        AccountDto result = accountService.update(updateDto);

        assertEquals(expectedDto, result);
        verify(accountMapper).updateUserFromUpdateAccountDto(updateDto, testUser);
        verify(userService).save(testUser);
        verify(accountMapper).userToAccountDto(testUser);
    }

    @Test
    @DisplayName("Should update email and set email as not verified")
    void shouldUpdateEmailAndSetEmailAsNotVerified() {
        UpdateAccountDto updateDto = UpdateAccountDto.builder()
                .username("testuser")
                .email("newemail@example.com")
                .fullName("Test User")
                .build();

        AccountDto expectedDto = new AccountDto();

        when(userService.existsByEmail(updateDto.getEmail())).thenReturn(false);
        when(accountMapper.userToAccountDto(testUser)).thenReturn(expectedDto);

        AccountDto result = accountService.update(updateDto);

        assertEquals(expectedDto, result);
        assertFalse(testUser.isEmailVerified());
        verify(accountMapper).updateUserFromUpdateAccountDto(updateDto, testUser);
        verify(userService).save(testUser);
        verify(accountMapper).userToAccountDto(testUser);
    }

    @Test
    @DisplayName("Should throw exception when updating email with social provider")
    void shouldThrowExceptionWhenUpdatingEmailWithSocialProvider() {
        testUser.setProvider(UserProvider.GOOGLE);

        UpdateAccountDto updateDto = UpdateAccountDto.builder()
                .username("testuser")
                .email("newemail@example.com")
                .fullName("Test User")
                .build();

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> accountService.update(updateDto));

        assertEquals("Cannot update email with a social provider", exception.getMessage());
        verify(userService, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when new email is already taken")
    void shouldThrowExceptionWhenNewEmailIsAlreadyTaken() {
        UpdateAccountDto updateDto = UpdateAccountDto.builder()
                .username("testuser")
                .email("taken@example.com")
                .fullName("Test User")
                .build();

        when(userService.existsByEmail(updateDto.getEmail())).thenReturn(true);

        ResourceConflictException exception = assertThrows(ResourceConflictException.class,
                () -> accountService.update(updateDto));

        assertEquals("Email already taken", exception.getMessage());
        verify(userService).existsByEmail(updateDto.getEmail());
        verify(userService, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when new username is already taken")
    void shouldThrowExceptionWhenNewUsernameIsAlreadyTaken() {
        UpdateAccountDto updateDto = UpdateAccountDto.builder()
                .username("taken")
                .email("test@example.com")
                .fullName("Test User")
                .build();

        when(userService.existsByUsername(updateDto.getUsername())).thenReturn(true);

        ResourceConflictException exception = assertThrows(ResourceConflictException.class,
                () -> accountService.update(updateDto));

        assertEquals("Username already taken", exception.getMessage());
        verify(userService).existsByUsername(updateDto.getUsername());
        verify(userService, never()).save(any());
    }

    @Test
    @DisplayName("Should delete account successfully")
    void shouldDeleteAccountSuccessfully() {
        accountService.delete();

        verify(userService).deleteById(userId);
    }

    @Test
    @DisplayName("Should update password successfully")
    void shouldUpdatePasswordSuccessfully() {
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        UpdatePasswordDto updatePasswordDto = UpdatePasswordDto.builder()
                .oldPassword(oldPassword)
                .newPassword(newPassword)
                .build();

        when(passwordEncoder.matches(oldPassword, testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encoded_new_password");

        accountService.updatePassword(updatePasswordDto);

        assertEquals("encoded_new_password", testUser.getPassword());
        verify(passwordEncoder).encode(newPassword);
        verify(userService).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception when updating password with social provider")
    void shouldThrowExceptionWhenUpdatingPasswordWithSocialProvider() {
        testUser.setProvider(UserProvider.GOOGLE);

        UpdatePasswordDto updatePasswordDto = UpdatePasswordDto.builder()
                .oldPassword("oldPassword")
                .newPassword("newPassword")
                .build();

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> accountService.updatePassword(updatePasswordDto));

        assertEquals("Cannot update password with a social provider", exception.getMessage());
        verify(userService, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when old password is incorrect")
    void shouldThrowExceptionWhenOldPasswordIsIncorrect() {
        UpdatePasswordDto updatePasswordDto = UpdatePasswordDto.builder()
                .oldPassword("wrongPassword")
                .newPassword("newPassword")
                .build();

        when(passwordEncoder.matches(updatePasswordDto.getOldPassword(), testUser.getPassword())).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> accountService.updatePassword(updatePasswordDto));

        assertEquals("Old password is incorrect", exception.getMessage());
        verify(passwordEncoder).matches(updatePasswordDto.getOldPassword(), testUser.getPassword());
        verify(userService, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when new password is same as old password")
    void shouldThrowExceptionWhenNewPasswordIsSameAsOldPassword() {
        String password = "samePassword";

        UpdatePasswordDto updatePasswordDto = UpdatePasswordDto.builder()
                .oldPassword(password)
                .newPassword(password)
                .build();

        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> accountService.updatePassword(updatePasswordDto));

        assertEquals("New password cannot be the same as old password", exception.getMessage());
        verify(passwordEncoder).matches(password, testUser.getPassword());
        verify(userService, never()).save(any());
    }

    @Test
    @DisplayName("Should send verification email")
    void shouldSendVerificationEmail() {
        SendActionEmailDto emailDto = SendActionEmailDto.builder()
                .email("test@example.com")
                .action(EmailSendingAction.VERIFY_EMAIL)
                .url("http://example.com")
                .build();

        accountService.sendActionEmail(emailDto);

        verify(emailVerifier).sendVerificationEmail(emailDto.getEmail(), emailDto.getUrl());
        verify(passwordResetter, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Should send password reset email")
    void shouldSendPasswordResetEmail() {
        SendActionEmailDto emailDto = SendActionEmailDto.builder()
                .email("test@example.com")
                .action(EmailSendingAction.RESET_PASSWORD)
                .url("http://example.com")
                .build();

        accountService.sendActionEmail(emailDto);

        verify(passwordResetter).sendPasswordResetEmail(emailDto.getEmail(), emailDto.getUrl());
        verify(emailVerifier, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Should verify email with token")
    void shouldVerifyEmailWithToken() {
        VerifyEmailDto verifyEmailDto = VerifyEmailDto.builder()
                .token("verification_token")
                .build();

        accountService.verifyEmail(verifyEmailDto);

        verify(emailVerifier).verify(verifyEmailDto.getToken());
    }

    @Test
    @DisplayName("Should reset password with token")
    void shouldResetPasswordWithToken() {
        PasswordResetDto resetDto = PasswordResetDto.builder()
                .token("reset_token")
                .password("new_password")
                .build();

        accountService.resetPassword(resetDto);

        verify(passwordResetter).resetPassword(resetDto.getToken(), resetDto.getPassword());
    }
}
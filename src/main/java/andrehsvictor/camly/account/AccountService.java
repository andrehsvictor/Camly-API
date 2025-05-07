package andrehsvictor.camly.account;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final JwtService jwtService;
    private final EmailVerifier emailVerifier;
    private final PasswordResetter passwordResetter;

    public AccountDto get() {
        return accountMapper.userToAccountDto(getCurrentUser());
    }

    public AccountDto create(CreateAccountDto createAccountDto) {
        validateUniqueFields(createAccountDto.getUsername(), createAccountDto.getEmail());

        User user = accountMapper.createAccountDtoToUser(createAccountDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user = userService.save(user);

        return accountMapper.userToAccountDto(user);
    }

    public AccountDto update(UpdateAccountDto updateAccountDto) {
        User user = getCurrentUser();
        String newEmail = updateAccountDto.getEmail();
        String newUsername = updateAccountDto.getUsername();

        validateFieldUpdates(user, newEmail, newUsername);

        accountMapper.updateUserFromUpdateAccountDto(updateAccountDto, user);
        userService.save(user);

        return accountMapper.userToAccountDto(user);
    }

    public void delete() {
        userService.deleteById(jwtService.getCurrentUserId());
    }

    public void updatePassword(UpdatePasswordDto updatePasswordDto) {
        User user = getCurrentUser();
        String oldPassword = updatePasswordDto.getOldPassword();
        String newPassword = updatePasswordDto.getNewPassword();

        validateLocalProvider(user, "password");
        validatePasswords(user, oldPassword, newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);
    }

    public void sendActionEmail(SendActionEmailDto dto) {
        String email = dto.getEmail();
        String url = dto.getUrl();

        switch (dto.getAction()) {
            case VERIFY_EMAIL -> emailVerifier.sendVerificationEmail(email, url);
            case RESET_PASSWORD -> passwordResetter.sendPasswordResetEmail(email, url);
        }
    }

    public void verifyEmail(VerifyEmailDto verifyEmailDto) {
        emailVerifier.verify(verifyEmailDto.getToken());
    }

    public void resetPassword(PasswordResetDto passwordResetDto) {
        passwordResetter.resetPassword(
                passwordResetDto.getToken(),
                passwordResetDto.getPassword());
    }

    private User getCurrentUser() {
        return userService.getById(jwtService.getCurrentUserId());
    }

    private void validateUniqueFields(String username, String email) {
        if (userService.existsByUsername(username)) {
            throw new ResourceConflictException("Username already taken");
        }
        if (userService.existsByEmail(email)) {
            throw new ResourceConflictException("Email already taken");
        }
    }

    private void validateFieldUpdates(User user, String newEmail, String newUsername) {
        if (!user.getEmail().equals(newEmail)) {
            validateLocalProvider(user, "email");
            if (userService.existsByEmail(newEmail)) {
                throw new ResourceConflictException("Email already taken");
            }
            user.setEmailVerified(false);
        }

        if (!user.getUsername().equals(newUsername) && userService.existsByUsername(newUsername)) {
            throw new ResourceConflictException("Username already taken");
        }
    }

    private void validateLocalProvider(User user, String fieldType) {
        if (user.getProvider() != UserProvider.LOCAL) {
            throw new BadRequestException("Cannot update " + fieldType + " with a social provider");
        }
    }

    private void validatePasswords(User user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }

        if (oldPassword.equals(newPassword)) {
            throw new BadRequestException("New password cannot be the same as old password");
        }
    }
}
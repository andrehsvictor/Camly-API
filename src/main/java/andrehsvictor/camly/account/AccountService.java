package andrehsvictor.camly.account;

import java.util.UUID;

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
        User user = getCurrentUser();
        return accountMapper.userToAccountDto(user);
    }

    public AccountDto create(CreateAccountDto createAccountDto) {
        validateUsernameAvailability(createAccountDto.getUsername());
        validateEmailAvailability(createAccountDto.getEmail());

        User user = accountMapper.createAccountDtoToUser(createAccountDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user = userService.save(user);

        return accountMapper.userToAccountDto(user);
    }

    public AccountDto update(UpdateAccountDto updateAccountDto) {
        User user = getCurrentUser();

        validateEmailUpdate(user, updateAccountDto.getEmail());
        validateUsernameUpdate(user, updateAccountDto.getUsername());

        accountMapper.updateUserFromUpdateAccountDto(updateAccountDto, user);
        userService.save(user);

        return accountMapper.userToAccountDto(user);
    }

    public void delete() {
        UUID userId = jwtService.getCurrentUserId();
        userService.deleteById(userId);
    }

    public void updatePassword(UpdatePasswordDto updatePasswordDto) {
        User user = getCurrentUser();
        validateUserProviderForPasswordUpdate(user);

        String oldPassword = updatePasswordDto.getOldPassword();
        String newPassword = updatePasswordDto.getNewPassword();

        validateOldPassword(user, oldPassword);
        validateNewPassword(oldPassword, newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);
    }

    public void sendActionEmail(SendActionEmailDto sendActionEmailDto) {
        switch (sendActionEmailDto.getAction()) {
            case VERIFY_EMAIL -> emailVerifier.sendVerificationEmail(
                    sendActionEmailDto.getEmail(), sendActionEmailDto.getUrl());
            case RESET_PASSWORD -> passwordResetter.sendPasswordResetEmail(
                    sendActionEmailDto.getEmail(), sendActionEmailDto.getUrl());
        }
    }

    public void verifyEmail(VerifyEmailDto verifyEmailDto) {
        emailVerifier.verify(verifyEmailDto.getToken());
    }

    public void resetPassword(PasswordResetDto passwordResetDto) {
        passwordResetter.resetPassword(passwordResetDto.getToken(), passwordResetDto.getPassword());
    }

    private User getCurrentUser() {
        UUID userId = jwtService.getCurrentUserId();
        return userService.getById(userId);
    }

    private void validateUsernameAvailability(String username) {
        if (userService.existsByUsername(username)) {
            throw new ResourceConflictException("Username already taken");
        }
    }

    private void validateEmailAvailability(String email) {
        if (userService.existsByEmail(email)) {
            throw new ResourceConflictException("Email already taken");
        }
    }

    private void validateEmailUpdate(User user, String newEmail) {
        boolean isEmailChanged = !user.getEmail().equals(newEmail);

        if (isEmailChanged) {
            validateUserProviderForEmailUpdate(user);
            validateEmailAvailability(newEmail);
            user.setEmailVerified(false);
        }
    }

    private void validateUserProviderForEmailUpdate(User user) {
        if (user.getProvider() != UserProvider.LOCAL) {
            throw new BadRequestException("Cannot update email with a social provider");
        }
    }

    private void validateUsernameUpdate(User user, String newUsername) {
        boolean isUsernameChanged = !user.getUsername().equals(newUsername);

        if (isUsernameChanged) {
            validateUsernameAvailability(newUsername);
        }
    }

    private void validateUserProviderForPasswordUpdate(User user) {
        if (user.getProvider() != UserProvider.LOCAL) {
            throw new BadRequestException("Cannot update your password with a social provider");
        }
    }

    private void validateOldPassword(User user, String oldPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }
    }

    private void validateNewPassword(String oldPassword, String newPassword) {
        if (oldPassword.equals(newPassword)) {
            throw new BadRequestException("New password cannot be the same as old password");
        }
    }
}
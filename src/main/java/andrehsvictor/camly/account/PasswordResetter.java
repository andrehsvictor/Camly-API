package andrehsvictor.camly.account;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import andrehsvictor.camly.email.EmailService;
import andrehsvictor.camly.exception.BadRequestException;
import andrehsvictor.camly.user.User;
import andrehsvictor.camly.user.UserService;
import andrehsvictor.camly.util.ClasspathFileService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetter {

    private static final String EMAIL_TEMPLATE_PATH = "templates/reset-password.html";
    private static final String EMAIL_SUBJECT = "Reset your password";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES_LENGTH = 32;

    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserService userService;
    private final ClasspathFileService classpathFileService;
    private final ActionTokenLifespanProperties tokenProperties;

    public void sendPasswordResetEmail(String email, String baseUrl) {
        User user = userService.getByEmail(email);

        String token = generateToken();
        LocalDateTime expiresAt = LocalDateTime.now().plus(tokenProperties.getPasswordResetTokenLifespan());

        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiresAt(expiresAt);
        userService.save(user);

        String resetUrl = baseUrl + (baseUrl.contains("?") ? "&" : "?") + "token=" + token;
        String template = classpathFileService.getContent(EMAIL_TEMPLATE_PATH);
        String expirationText = formatDuration(tokenProperties.getPasswordResetTokenLifespan());

        String emailBody = template
                .replace("{{link}}", resetUrl)
                .replace("{{expiration}}", expirationText);

        emailService.send(email, EMAIL_SUBJECT, emailBody);
    }

    public void resetPassword(String token, String newPassword) {
        User user = userService.getByResetPasswordToken(token);
        if (user.getResetPasswordTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Action token expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiresAt(null);
        userService.save(user);
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        if (hours > 0) {
            String hourText = hours + (hours == 1 ? " hour" : " hours");
            return minutes > 0 ? hourText + " and " + minutes + (minutes == 1 ? " minute" : " minutes") : hourText;
        } else {
            long totalMinutes = duration.toMinutes();
            return totalMinutes + (totalMinutes == 1 ? " minute" : " minutes");
        }
    }

    private String generateToken() {
        byte[] randomBytes = new byte[TOKEN_BYTES_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
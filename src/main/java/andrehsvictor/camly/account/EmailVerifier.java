package andrehsvictor.camly.account;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.stereotype.Service;

import andrehsvictor.camly.email.EmailService;
import andrehsvictor.camly.exception.BadRequestException;
import andrehsvictor.camly.exception.ResourceConflictException;
import andrehsvictor.camly.user.User;
import andrehsvictor.camly.user.UserService;
import andrehsvictor.camly.util.ClasspathFileService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerifier {

    private static final String EMAIL_TEMPLATE_PATH = "templates/verify-email.html";
    private static final String EMAIL_SUBJECT = "Verify your email";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final EmailService emailService;
    private final UserService userService;
    private final ClasspathFileService classpathFileService;
    private final ActionTokenLifespanProperties actionTokenLifespanProperties;

    public void sendVerificationEmail(String email, String url) {
        User user = userService.getByEmail(email);
        if (user.isEmailVerified()) {
            throw new ResourceConflictException("Email already verified");
        }

        String token = generateToken();
        LocalDateTime expiresAt = generateTokenExpiration();

        user.setEmailVerificationToken(token);
        user.setEmailVerificationTokenExpiresAt(expiresAt);
        userService.save(user);

        String verificationUrl = buildVerificationUrl(url, token);
        String emailBody = prepareEmailBody(verificationUrl);

        emailService.send(email, EMAIL_SUBJECT, emailBody);
    }

    public void verify(String token) {
        User user = userService.getByEmailVerificationToken(token);
        if (user.getEmailVerificationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Action token expired");
        }
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiresAt(null);
        userService.save(user);
    }

    private String buildVerificationUrl(String baseUrl, String token) {
        return baseUrl + (baseUrl.contains("?") ? "&" : "?") + "token=" + token;
    }

    private String prepareEmailBody(String verificationUrl) {
        String template = classpathFileService.getContent(EMAIL_TEMPLATE_PATH);
        Duration lifespan = actionTokenLifespanProperties.getEmailVerificationTokenLifespan();

        String expirationText = formatExpirationTime(lifespan);

        return template
                .replace("{{link}}", verificationUrl)
                .replace("{{expiration}}", expirationText);
    }

    private String formatExpirationTime(Duration duration) {
        long hours = duration.toHours();
        long remainingMinutes = duration.toMinutesPart();

        if (hours > 0) {
            return hours + (hours == 1 ? " hour" : " hours") +
                    (remainingMinutes > 0 ? " and " + remainingMinutes +
                            (remainingMinutes == 1 ? " minute" : " minutes") : "");
        } else {
            return duration.toMinutes() +
                    (duration.toMinutes() == 1 ? " minute" : " minutes");
        }
    }

    private String generateToken() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private LocalDateTime generateTokenExpiration() {
        return LocalDateTime.now().plus(actionTokenLifespanProperties.getEmailVerificationTokenLifespan());
    }
}
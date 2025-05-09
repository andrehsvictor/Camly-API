package andrehsvictor.camly.account;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import andrehsvictor.camly.account.dto.SendActionEmailDto;
import andrehsvictor.camly.account.dto.VerifyEmailDto;
import andrehsvictor.camly.user.User;

class AccountEmailVerificationIT extends AbstractAccountIntegrationTest {

    @Test
    @DisplayName("Should send verification email")
    void shouldSendVerificationEmail() {
        SendActionEmailDto emailDto = SendActionEmailDto.builder()
                .email(testUser.getEmail())
                .action(EmailSendingAction.VERIFY_EMAIL)
                .url("http://localhost:3000")
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(emailDto)
                .when()
                .post("/api/v1/account/send-action-email")
                .then()
                .statusCode(HttpStatus.OK.value());

        User updatedUser = userService.getById(testUser.getId());
        assertTrue(updatedUser.getEmailVerificationToken() != null);
        assertTrue(updatedUser.getEmailVerificationTokenExpiresAt() != null);
        assertTrue(updatedUser.getEmailVerificationTokenExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Should verify email successfully")
    void shouldVerifyEmailSuccessfully() {
        String token = "test_token_" + System.currentTimeMillis();
        testUser.setEmailVerificationToken(token);
        testUser.setEmailVerified(false);
        testUser.setEmailVerificationTokenExpiresAt(LocalDateTime.now().plusHours(1));
        userService.save(testUser);

        VerifyEmailDto verifyDto = VerifyEmailDto.builder()
                .token(token)
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(verifyDto)
                .when()
                .post("/api/v1/account/verify")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        User updatedUser = userService.getById(testUser.getId());
        assertTrue(updatedUser.isEmailVerified());
        assertTrue(updatedUser.getEmailVerificationToken() == null);
        assertTrue(updatedUser.getEmailVerificationTokenExpiresAt() == null);
    }
}
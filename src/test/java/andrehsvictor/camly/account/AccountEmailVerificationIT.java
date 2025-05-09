package andrehsvictor.camly.account;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
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
        User user = userService.getById(testUser.getId());
        user.setEmailVerified(false);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiresAt(null);
        userService.save(user);

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

        when().get(String.format("%s/api/v1/messages", getMailHogUrl()))
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", equalTo(1))
                .body("[0].To[0].Mailbox", equalTo(testUser.getEmail().split("@")[0]))
                .body("[0].To[0].Domain", equalTo(testUser.getEmail().split("@")[1]))
                .body("[0].Content.Headers.Subject[0]", equalTo("Verify your email"))
                .body("[0].Content.Headers.From[0]", equalTo("noreply@camly.io"))
                .body("[0].Content.Body", containsString("token="))
                .body("[0].Content.Body", containsString("Verify My Email"));
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
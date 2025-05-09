package andrehsvictor.camly.account;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import andrehsvictor.camly.account.dto.PasswordResetDto;
import andrehsvictor.camly.account.dto.SendActionEmailDto;
import andrehsvictor.camly.account.dto.UpdatePasswordDto;
import andrehsvictor.camly.token.dto.UsernamePasswordDto;
import andrehsvictor.camly.user.User;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

class AccountPasswordIT extends AbstractAccountIntegrationTest {

    @Test
    @DisplayName("Should send password reset email")
    void shouldSendPasswordResetEmail() {
        SendActionEmailDto emailDto = SendActionEmailDto.builder()
                .email(testUser.getEmail())
                .action(EmailSendingAction.RESET_PASSWORD)
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
        assertTrue(updatedUser.getResetPasswordToken() != null);
        assertTrue(updatedUser.getResetPasswordTokenExpiresAt() != null);
        assertTrue(updatedUser.getResetPasswordTokenExpiresAt().isAfter(LocalDateTime.now()));

        when().get(getMailHogUrl() + "/api/v1/messages")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", equalTo(1))
                .body("[0].To[0].Mailbox", equalTo(testUser.getEmail().split("@")[0]))
                .body("[0].To[0].Domain", equalTo(testUser.getEmail().split("@")[1]))
                .body("[0].Content.Headers.Subject[0]", equalTo("Reset your password"))
                .body("[0].Content.Headers.From[0]", equalTo("noreply@camly.io"))
                .body("[0].Content.Body", containsString("token="))
                .body("[0].Content.Body", containsString("Reset My Password"));
    }

    @Test
    @DisplayName("Should reset password successfully")
    void shouldResetPasswordSuccessfully() {
        String token = "reset_token_" + System.currentTimeMillis();
        String oldPasswordHash = testUser.getPassword();
        testUser.setResetPasswordToken(token);
        testUser.setResetPasswordTokenExpiresAt(LocalDateTime.now().plusHours(1));
        userService.save(testUser);

        String newPassword = "NewPassword@123";
        PasswordResetDto resetDto = PasswordResetDto.builder()
                .token(token)
                .password(newPassword)
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(resetDto)
                .when()
                .post("/api/v1/account/reset-password")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        User updatedUser = userService.getById(testUser.getId());
        assertTrue(updatedUser.getResetPasswordToken() == null);
        assertTrue(updatedUser.getResetPasswordTokenExpiresAt() == null);
        assertTrue(!updatedUser.getPassword().equals(oldPasswordHash));

        UsernamePasswordDto credentials = UsernamePasswordDto.builder()
                .username(testUser.getEmail())
                .password(newPassword)
                .build();

        ValidatableResponse response = given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/api/v1/token")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("tokenType", equalTo("Bearer"))
                .body("expiresIn", greaterThan(0));

        String newToken = response.extract().path("accessToken");
        given()
                .header("Authorization", "Bearer " + newToken)
                .when()
                .get("/api/v1/account")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Should update password successfully")
    void shouldUpdatePasswordSuccessfully() {
        String newPassword = "NewPassword@123";
        String currentPassword = "Test@123456";
        String oldPasswordHash = testUser.getPassword();

        UpdatePasswordDto passwordDto = UpdatePasswordDto.builder()
                .oldPassword(currentPassword)
                .newPassword(newPassword)
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .body(passwordDto)
                .when()
                .put("/api/v1/account/password")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        User updatedUser = userService.getById(testUser.getId());
        assertTrue(!updatedUser.getPassword().equals(oldPasswordHash));

        UsernamePasswordDto credentials = UsernamePasswordDto.builder()
                .username(testUser.getEmail())
                .password(newPassword)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/api/v1/token")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("tokenType", equalTo("Bearer"))
                .body("expiresIn", greaterThan(0));

        UsernamePasswordDto oldCredentials = UsernamePasswordDto.builder()
                .username(testUser.getEmail())
                .password(currentPassword)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(oldCredentials)
                .when()
                .post("/api/v1/token")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }
}
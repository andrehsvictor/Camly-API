package andrehsvictor.camly.account;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import andrehsvictor.camly.AbstractIT;
import andrehsvictor.camly.account.dto.CreateAccountDto;
import andrehsvictor.camly.account.dto.PasswordResetDto;
import andrehsvictor.camly.account.dto.SendActionEmailDto;
import andrehsvictor.camly.account.dto.UpdateAccountDto;
import andrehsvictor.camly.account.dto.UpdatePasswordDto;
import andrehsvictor.camly.account.dto.VerifyEmailDto;
import andrehsvictor.camly.token.TokenService;
import andrehsvictor.camly.token.dto.TokenDto;
import andrehsvictor.camly.token.dto.UsernamePasswordDto;
import andrehsvictor.camly.user.User;
import andrehsvictor.camly.user.UserService;
import io.restassured.http.ContentType;

class AccountControllerIT extends AbstractIT {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    private String accessToken;
    private User testUser;

    @BeforeEach
    void setupTestUser() {
        CreateAccountDto createAccountDto = CreateAccountDto.builder()
                .fullName("Test User")
                .username("testuser" + System.currentTimeMillis()) // Ensure uniqueness
                .email("test" + System.currentTimeMillis() + "@example.com") // Ensure uniqueness
                .password("Test@123456")
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(createAccountDto)
                .when()
                .post("/api/v1/account")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        User createdUser = userService.getByEmail(createAccountDto.getEmail());
        createdUser.setEmailVerified(true);
        userService.save(createdUser);

        UsernamePasswordDto credentials = UsernamePasswordDto.builder()
                .username(createAccountDto.getEmail())
                .password(createAccountDto.getPassword())
                .build();

        TokenDto tokenDto = tokenService.request(credentials);
        accessToken = tokenDto.getAccessToken();

        testUser = userService.getByEmail(createAccountDto.getEmail());
    }

    @Test
    @DisplayName("Should create a new account successfully")
    void shouldCreateAccountSuccessfully() {
        CreateAccountDto createAccountDto = CreateAccountDto.builder()
                .fullName("New Test User")
                .username("newuser" + System.currentTimeMillis())
                .email("newuser" + System.currentTimeMillis() + "@example.com")
                .password("Test@123456")
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(createAccountDto)
                .when()
                .post("/api/v1/account")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("username", equalTo(createAccountDto.getUsername()))
                .body("email", equalTo(createAccountDto.getEmail()))
                .body("fullName", equalTo(createAccountDto.getFullName()))
                .body("emailVerified", equalTo(false))
                .body("bio", equalTo(null))
                .body("pictureUrl", equalTo(null))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue());

    }

    @Test
    @DisplayName("Should return conflict when creating account with existing username")
    void shouldReturnConflictWhenUsernameExists() {
        CreateAccountDto createAccountDto = CreateAccountDto.builder()
                .fullName("Duplicate Username")
                .username(testUser.getUsername())
                .email("unique" + System.currentTimeMillis() + "@example.com")
                .password("Test@123456")
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(createAccountDto)
                .when()
                .post("/api/v1/account")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    @DisplayName("Should return conflict when creating account with existing email")
    void shouldReturnConflictWhenEmailExists() {
        CreateAccountDto createAccountDto = CreateAccountDto.builder()
                .fullName("Duplicate Email")
                .username("unique" + System.currentTimeMillis())
                .email(testUser.getEmail())
                .password("Test@123456")
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(createAccountDto)
                .when()
                .post("/api/v1/account")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    @DisplayName("Should get current account details")
    void shouldGetCurrentAccount() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/account")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(testUser.getId().toString()))
                .body("username", equalTo(testUser.getUsername()))
                .body("email", equalTo(testUser.getEmail()));
    }

    @Test
    @DisplayName("Should update account successfully")
    void shouldUpdateAccountSuccessfully() {
        String newBio = "New bio for testing purposes";
        UpdateAccountDto updateDto = UpdateAccountDto.builder()
                .bio(newBio)
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .body(updateDto)
                .when()
                .put("/api/v1/account")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("bio", equalTo(newBio));
    }

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
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

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
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("Should verify email successfully")
    void shouldVerifyEmailSuccessfully() {
        // Generate an email verification token for the test user
        String token = "test_token_" + System.currentTimeMillis();
        testUser.setEmailVerificationToken(token);
        testUser.setEmailVerificationTokenExpiresAt(java.time.LocalDateTime.now().plusHours(1));
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

        // Verify that the user's email is now verified
        User updatedUser = userService.getById(testUser.getId());
        assert updatedUser.isEmailVerified();
    }

    @Test
    @DisplayName("Should reset password successfully")
    void shouldResetPasswordSuccessfully() {
        // Generate a password reset token for the test user
        String token = "reset_token_" + System.currentTimeMillis();
        testUser.setResetPasswordToken(token);
        testUser.setResetPasswordTokenExpiresAt(java.time.LocalDateTime.now().plusHours(1));
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

        // Verify login with new password works
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
                .body("refreshToken", notNullValue());
    }

    @Test
    @DisplayName("Should update password successfully")
    void shouldUpdatePasswordSuccessfully() {
        String newPassword = "NewPassword@123";
        String currentPassword = "Test@123456"; // From setup

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

        // Verify login with new password works
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
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Should delete account successfully")
    void shouldDeleteAccountSuccessfully() {
        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .delete("/api/v1/account")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // Verify the account is deleted by attempting to get it (should fail)
        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/account")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }
}
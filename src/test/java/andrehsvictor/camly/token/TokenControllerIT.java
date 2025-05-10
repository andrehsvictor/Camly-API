package andrehsvictor.camly.token;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import andrehsvictor.camly.AbstractIntegrationTest;
import andrehsvictor.camly.account.dto.CreateAccountDto;
import andrehsvictor.camly.token.dto.IdTokenDto;
import andrehsvictor.camly.token.dto.RefreshTokenDto;
import andrehsvictor.camly.token.dto.RevokeTokenDto;
import andrehsvictor.camly.token.dto.TokenDto;
import andrehsvictor.camly.token.dto.UsernamePasswordDto;
import andrehsvictor.camly.user.User;
import andrehsvictor.camly.user.UserService;
import io.restassured.http.ContentType;

public class TokenControllerIT extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    private User testUser;
    private String username;
    private String password;

    @BeforeEach
    void setupTestUser() {
        username = "testuser" + System.currentTimeMillis();
        password = "Test@123456";

        CreateAccountDto createAccountDto = CreateAccountDto.builder()
                .fullName("Test User")
                .username(username)
                .email(username + "@example.com")
                .password(password)
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(createAccountDto)
                .when()
                .post("/api/v1/account")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        testUser = userService.getByEmail(createAccountDto.getEmail());

        testUser.setEmailVerified(true);
        userService.save(testUser);
    }

    @Test
    @DisplayName("Should request token with valid credentials")
    void shouldRequestTokenWithValidCredentials() {
        UsernamePasswordDto credentials = UsernamePasswordDto.builder()
                .username(testUser.getEmail())
                .password(password)
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
    }

    @Test
    @DisplayName("Should return unauthorized with invalid credentials")
    void shouldReturnUnauthorizedWithInvalidCredentials() {
        UsernamePasswordDto credentials = UsernamePasswordDto.builder()
                .username(testUser.getEmail())
                .password("WrongPassword123")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/api/v1/token")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("status", equalTo(HttpStatus.UNAUTHORIZED.value()))
                .body("message", containsString("Bad credentials"))
                .body("timestamp", notNullValue());
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void shouldRefreshTokenSuccessfully() {
        UsernamePasswordDto credentials = UsernamePasswordDto.builder()
                .username(testUser.getEmail())
                .password(password)
                .build();

        TokenDto initialToken = given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/api/v1/token")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(TokenDto.class);

        RefreshTokenDto refreshTokenDto = RefreshTokenDto.builder()
                .refreshToken(initialToken.getRefreshToken())
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(refreshTokenDto)
                .when()
                .post("/api/v1/token/refresh")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("tokenType", equalTo("Bearer"))
                .body("expiresIn", greaterThan(0))
                .body("accessToken", not(equalTo(initialToken.getAccessToken())));
    }

    @Test
    @DisplayName("Should return unauthorized with invalid refresh token")
    void shouldReturnUnauthorizedWithInvalidRefreshToken() {
        RefreshTokenDto refreshTokenDto = RefreshTokenDto.builder()
                .refreshToken("invalid_refresh_token")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(refreshTokenDto)
                .when()
                .post("/api/v1/token/refresh")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("status", equalTo(HttpStatus.UNAUTHORIZED.value()))
                .body("message", containsString("Invalid refresh token"))
                .body("timestamp", notNullValue());
    }

    @Test
    @DisplayName("Should revoke token successfully")
    void shouldRevokeTokenSuccessfully() {
        UsernamePasswordDto credentials = UsernamePasswordDto.builder()
                .username(testUser.getEmail())
                .password(password)
                .build();

        TokenDto initialToken = given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/api/v1/token")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(TokenDto.class);

        RevokeTokenDto revokeTokenDto = RevokeTokenDto.builder()
                .token(initialToken.getRefreshToken())
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(revokeTokenDto)
                .when()
                .post("/api/v1/token/revoke")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        RefreshTokenDto refreshTokenDto = RefreshTokenDto.builder()
                .refreshToken(initialToken.getRefreshToken())
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(refreshTokenDto)
                .when()
                .post("/api/v1/token/refresh")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("status", equalTo(HttpStatus.UNAUTHORIZED.value()))
                .body("message", containsString("Invalid refresh token"))
                .body("timestamp", notNullValue());
    }

    @Test
    @DisplayName("Should return bad request with invalid token format")
    void shouldReturnBadRequestWithInvalidTokenFormat() {
        UsernamePasswordDto credentials = UsernamePasswordDto.builder()
                .username("")
                .password("")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/api/v1/token")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("message", containsString("Validation failed"))
                .body("timestamp", notNullValue());
    }

    @Test
    @DisplayName("Should handle Google authentication")
    void shouldHandleGoogleAuthentication() {
        IdTokenDto idTokenDto = IdTokenDto.builder()
                .idToken("mock_google_id_token")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(idTokenDto)
                .when()
                .post("/api/v1/token/google")
                .then()
                .statusCode(not(equalTo(HttpStatus.OK.value())));
    }
}
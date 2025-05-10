package andrehsvictor.camly.token;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

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

    @MockitoBean
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    private User testUser;
    private String username;
    private String password;
    private final String MOCK_ID_TOKEN = "mock_google_id_token";
    private final String TEST_EMAIL = "google_user@gmail.com";
    private final String TEST_SUBJECT = "google_user_id_123456";
    private final String TEST_NAME = "Google Test User";
    private final String TEST_PICTURE = "https://lh3.googleusercontent.com/test_picture";

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

        try {
            GoogleIdToken mockIdToken = mock(GoogleIdToken.class);
            GoogleIdToken.Payload mockPayload = new GoogleIdToken.Payload();

            mockPayload.setEmail(TEST_EMAIL);
            mockPayload.setEmailVerified(true);
            mockPayload.setSubject(TEST_SUBJECT);
            mockPayload.set("name", TEST_NAME);
            mockPayload.set("picture", TEST_PICTURE);

            when(mockIdToken.getPayload()).thenReturn(mockPayload);

            when(googleIdTokenVerifier.verify(MOCK_ID_TOKEN)).thenReturn(mockIdToken);
            when(googleIdTokenVerifier.verify(anyString())).thenReturn(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up Google mock", e);
        }
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
                .body("message", containsString("Invalid credentials"))
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
    @DisplayName("Should return bad request with invalid refresh token format")
    void shouldReturnBadRequestWithInvalidRefreshTokenFormat() {
        RefreshTokenDto refreshTokenDto = RefreshTokenDto.builder()
                .refreshToken("invalid_refresh_token")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(refreshTokenDto)
                .when()
                .post("/api/v1/token/refresh")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("message", containsString("Validation failed"))
                .body("errors[0].field", equalTo("refreshToken"))
                .body("errors[0].message", containsString("Refresh token should be a valid JWT"))
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

        RevokeTokenDto revokeAccessTokenDto = RevokeTokenDto.builder()
                .token(initialToken.getAccessToken())
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(revokeAccessTokenDto)
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
                .body("message",
                        containsString(
                                "An error occurred while attempting to decode the Jwt: The token has been revoked"))
                .body("timestamp", notNullValue());

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + initialToken.getAccessToken())
                .when()
                .get("/api/v1/account")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
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
    @DisplayName("Should return error with invalid Google ID token")
    void shouldReturnErrorWithInvalidGoogleIdToken() {
        IdTokenDto idTokenDto = IdTokenDto.builder()
                .idToken("invalid_google_token")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(idTokenDto)
                .when()
                .post("/api/v1/token/google")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
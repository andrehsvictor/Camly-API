package andrehsvictor.camly.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import andrehsvictor.camly.AbstractIntegrationTest;
import andrehsvictor.camly.account.dto.CreateAccountDto;
import andrehsvictor.camly.token.TokenService;
import andrehsvictor.camly.token.dto.TokenDto;
import andrehsvictor.camly.token.dto.UsernamePasswordDto;

public class UserControllerIT extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    private String accessToken;
    private User testUser;
    private User secondUser;

    @BeforeEach
    void setupTestUsers() {
        CreateAccountDto firstUser = CreateAccountDto.builder()
                .fullName("Test User")
                .username("testuser" + System.currentTimeMillis())
                .email("test" + System.currentTimeMillis() + "@example.com")
                .password("Test@123456")
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(firstUser)
                .when()
                .post("/api/v1/account")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        CreateAccountDto secondUserDto = CreateAccountDto.builder()
                .fullName("Second User")
                .username("seconduser" + System.currentTimeMillis())
                .email("second" + System.currentTimeMillis() + "@example.com")
                .password("Test@123456")
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(secondUserDto)
                .when()
                .post("/api/v1/account")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        testUser = userService.getByEmail(firstUser.getEmail());
        secondUser = userService.getByEmail(secondUserDto.getEmail());

        testUser.setEmailVerified(true);
        userService.save(testUser);

        UsernamePasswordDto credentials = UsernamePasswordDto.builder()
                .username(firstUser.getEmail())
                .password(firstUser.getPassword())
                .build();

        TokenDto tokenDto = tokenService.request(credentials);
        accessToken = tokenDto.getAccessToken();
    }

    @Test
    @DisplayName("Should get user by id")
    void shouldGetUserById() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/users/" + secondUser.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(secondUser.getId().toString()))
                .body("username", equalTo(secondUser.getUsername()))
                .body("fullName", equalTo(secondUser.getFullName()))
                .body("email", nullValue()) // Email não deve ser exposto para outros usuários
                .body("followerCount", equalTo(0))
                .body("followingCount", equalTo(0))
                .body("postCount", equalTo(0))
                .body("createdAt", notNullValue())
                .body("$", not(hasKey("password")));
    }

    @Test
    @DisplayName("Should return not found for invalid user id")
    void shouldReturnNotFoundForInvalidUserId() {
        UUID invalidId = UUID.randomUUID();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/users/" + invalidId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", containsString("User not found"))
                .body("timestamp", notNullValue());
    }

    @Test
    @DisplayName("Should get users with pagination")
    void shouldGetUsersWithPagination() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/users?page=0&size=10")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(greaterThan(1)))
                .body("pageable", notNullValue())
                .body("totalElements", greaterThan(1))
                .body("totalPages", greaterThan(0))
                .body("content[0].id", notNullValue())
                .body("content[0].username", notNullValue())
                .body("content[0].fullName", notNullValue());
    }

    @Test
    @DisplayName("Should filter users by query parameter")
    void shouldFilterUsersByQuery() {
        String uniqueUsername = secondUser.getUsername();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/users?q=" + uniqueUsername)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(greaterThan(0)))
                .body("content.findAll { it.username == '" + uniqueUsername + "' }.size()", greaterThan(0));
    }

    @Test
    @DisplayName("Should follow a user successfully")
    void shouldFollowUserSuccessfully() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .put("/api/v1/users/" + secondUser.getId() + "/followers")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/users/" + secondUser.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("followerCount", equalTo(1));

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/account")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("followingCount", equalTo(1));
    }

    @Test
    @DisplayName("Should unfollow a user when following twice")
    void shouldUnfollowUserWhenFollowingTwice() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .put("/api/v1/users/" + secondUser.getId() + "/followers")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .put("/api/v1/users/" + secondUser.getId() + "/followers")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/users/" + secondUser.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("followerCount", equalTo(0));
    }

    @Test
    @DisplayName("Should return unauthorized when accessing without token")
    void shouldReturnUnauthorizedWhenAccessingWithoutToken() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get("/api/v1/users")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("Should return not found when trying to follow non-existent user")
    void shouldReturnNotFoundWhenTryingToFollowNonExistentUser() {
        UUID nonExistentId = UUID.randomUUID();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .put("/api/v1/users/" + nonExistentId + "/followers")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", containsString("User not found"))
                .body("timestamp", notNullValue());
    }
}
package andrehsvictor.camly.account;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import andrehsvictor.camly.account.dto.UpdateAccountDto;
import andrehsvictor.camly.exception.ResourceNotFoundException;
import andrehsvictor.camly.user.User;

class AccountProfileIT extends AbstractAccountIntegrationTest {

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
                .body("email", equalTo(testUser.getEmail()))
                .body("fullName", equalTo(testUser.getFullName()))
                .body("emailVerified", equalTo(true))
                .body("bio", equalTo(testUser.getBio()))
                .body("pictureUrl", equalTo(testUser.getPictureUrl()))
                .body("followerCount", equalTo(testUser.getFollowerCount()))
                .body("followingCount", equalTo(testUser.getFollowingCount()))
                .body("postCount", equalTo(testUser.getPostCount()))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue())
                .body("$", not(hasKey("password")));
    }

    @Test
    @DisplayName("Should update account successfully")
    void shouldUpdateAccountSuccessfully() {
        String newBio = "New bio for testing purposes";
        String newFullName = "Updated Test User";
        UpdateAccountDto updateDto = UpdateAccountDto.builder()
                .bio(newBio)
                .fullName(newFullName)
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .body(updateDto)
                .when()
                .put("/api/v1/account")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(testUser.getId().toString()))
                .body("username", equalTo(testUser.getUsername()))
                .body("email", equalTo(testUser.getEmail()))
                .body("fullName", equalTo(newFullName))
                .body("bio", equalTo(newBio))
                .body("updatedAt", notNullValue())
                .body("updatedAt", not(equalTo(testUser.getUpdatedAt().toString())))
                .body("$", not(hasKey("password")));

        User updatedUser = userService.getById(testUser.getId());
        assertTrue(updatedUser.getBio().equals(newBio));
        assertTrue(updatedUser.getFullName().equals(newFullName));
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

        assertThatThrownBy(() -> userService.getById(testUser.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
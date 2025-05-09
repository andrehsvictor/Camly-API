package andrehsvictor.camly.account;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import andrehsvictor.camly.account.dto.CreateAccountDto;

class AccountCreationIT extends AbstractAccountIntegrationTest {

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
                .body("id", notNullValue())
                .body("username", equalTo(createAccountDto.getUsername()))
                .body("email", equalTo(createAccountDto.getEmail()))
                .body("fullName", equalTo(createAccountDto.getFullName()))
                .body("emailVerified", equalTo(false))
                .body("bio", nullValue())
                .body("pictureUrl", nullValue())
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue())
                .body("followerCount", equalTo(0))
                .body("followingCount", equalTo(0))
                .body("postCount", equalTo(0))
                .body("createdAt", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?.*"))
                .body("$", not(hasKey("password")))
                .body("provider", equalTo("LOCAL"));
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
                .statusCode(HttpStatus.CONFLICT.value())
                .body("status", equalTo(HttpStatus.CONFLICT.value()))
                .body("message", containsString("Username already taken"))
                .body("timestamp", notNullValue())
                .body("requestId", notNullValue());
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
                .statusCode(HttpStatus.CONFLICT.value())
                .body("status", equalTo(HttpStatus.CONFLICT.value()))
                .body("message", containsString("Email already taken"))
                .body("timestamp", notNullValue())
                .body("requestId", notNullValue());
    }
}
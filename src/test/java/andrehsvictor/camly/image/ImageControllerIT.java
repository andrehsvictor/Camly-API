package andrehsvictor.camly.image;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
import andrehsvictor.camly.user.User;
import andrehsvictor.camly.user.UserService;

class ImageControllerIT extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    private String accessToken;
    private User testUser;

    @BeforeEach
    void setupTestUser() {
        String username = "imageuser" + System.currentTimeMillis();
        String password = "Test@123456";

        CreateAccountDto accountDto = CreateAccountDto.builder()
                .fullName("Image Test User")
                .username(username)
                .email(username + "@example.com")
                .password(password)
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(accountDto)
                .when()
                .post("/api/v1/account")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        testUser = userService.getByEmail(accountDto.getEmail());
        testUser.setEmailVerified(true);
        userService.save(testUser);

        UsernamePasswordDto credentials = UsernamePasswordDto.builder()
                .username(accountDto.getEmail())
                .password(password)
                .build();

        TokenDto tokenDto = tokenService.request(credentials);
        accessToken = tokenDto.getAccessToken();
    }

    @Test
    @DisplayName("Should upload image successfully")
    void shouldUploadImageSuccessfully() throws IOException {
        File testImage = File.createTempFile("test-image", ".jpg");
        Files.write(testImage.toPath(), "test image content".getBytes());

        String url = given()
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("file", testImage, "image/jpeg")
                .when()
                .post("/api/v1/images")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("url", notNullValue())
                .extract()
                .path("url");

        given()
                .when()
                .get(url)
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(MediaType.IMAGE_JPEG_VALUE)
                .body(notNullValue());
    }

    @Test
    @DisplayName("Should fail to upload without authentication")
    void shouldFailToUploadWithoutAuthentication() throws IOException {
        File testImage = File.createTempFile("test-image", ".jpg");
        Files.write(testImage.toPath(), "test image content".getBytes());

        given()
                .multiPart("file", testImage, "image/jpeg")
                .when()
                .post("/api/v1/images")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("Should fail to upload with empty file")
    void shouldFailToUploadWithEmptyFile() {
        given()
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("file", "", "image/jpeg")
                .when()
                .post("/api/v1/images")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Should fail to upload with invalid file type")
    void shouldFailToUploadWithInvalidFileType() throws IOException {
        File testFile = File.createTempFile("test-file", ".txt");
        Files.write(testFile.toPath(), "this is not an image".getBytes());

        given()
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("file", testFile, "text/plain")
                .when()
                .post("/api/v1/images")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Should fail to upload with file too large")
    void shouldFailToUploadWithFileTooLarge() throws IOException {
        File largeFile = File.createTempFile("large-image", ".jpg");
        byte[] largeContent = new byte[10 * 1024 * 1024];
        Files.write(largeFile.toPath(), largeContent);

        given()
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("file", largeFile, "image/jpeg")
                .when()
                .post("/api/v1/images")
                .then()
                .statusCode(HttpStatus.PAYLOAD_TOO_LARGE.value());
    }
}
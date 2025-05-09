package andrehsvictor.camly.account;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.BeforeEach;
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

public abstract class AbstractAccountIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    protected UserService userService;

    @Autowired
    protected TokenService tokenService;

    protected String accessToken;
    protected User testUser;

    @BeforeEach
    void setupTestUser() {
        CreateAccountDto createAccountDto = CreateAccountDto.builder()
                .fullName("Test User")
                .username("testuser" + System.currentTimeMillis())
                .email("test" + System.currentTimeMillis() + "@example.com")
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
}
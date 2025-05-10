package andrehsvictor.camly.post;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import andrehsvictor.camly.AbstractIntegrationTest;
import andrehsvictor.camly.account.dto.CreateAccountDto;
import andrehsvictor.camly.post.dto.CreatePostDto;
import andrehsvictor.camly.post.dto.PostDto;
import andrehsvictor.camly.post.dto.UpdatePostDto;
import andrehsvictor.camly.token.TokenService;
import andrehsvictor.camly.token.dto.TokenDto;
import andrehsvictor.camly.token.dto.UsernamePasswordDto;
import andrehsvictor.camly.user.User;
import andrehsvictor.camly.user.UserService;

public class PostControllerIT extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PostService postService;

    private String accessToken;
    private User testUser;
    private UUID postId;

    @BeforeEach
    void setupTestUserAndPost() {
        // Create test user
        String username = "postuser" + System.currentTimeMillis();
        String password = "Test@123456";

        CreateAccountDto accountDto = CreateAccountDto.builder()
                .fullName("Post Test User")
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

        // Get token for authentication
        UsernamePasswordDto credentials = UsernamePasswordDto.builder()
                .username(accountDto.getEmail())
                .password(password)
                .build();

        TokenDto tokenDto = tokenService.request(credentials);
        accessToken = tokenDto.getAccessToken();

        // Create a test post
        CreatePostDto createPostDto = CreatePostDto.builder()
                .caption("Test post")
                .imageUrl("https://example.com/image.jpg")
                .build();

        PostDto postDto = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .body(createPostDto)
                .when()
                .post("/api/v1/posts")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(PostDto.class);

        postId = UUID.fromString(postDto.getId());
    }

    @Test
    @DisplayName("Should create post successfully")
    void shouldCreatePostSuccessfully() {
        CreatePostDto createPostDto = CreatePostDto.builder()
                .caption("New test post")
                .imageUrl("https://example.com/new-image.jpg")
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .body(createPostDto)
                .when()
                .post("/api/v1/posts")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("caption", equalTo(createPostDto.getCaption()))
                .body("imageUrl", equalTo(createPostDto.getImageUrl()))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue())
                .body("user.id", equalTo(testUser.getId().toString()))
                .body("user.username", equalTo(testUser.getUsername()))
                .body("likeCount", equalTo(0))
                .body("liked", equalTo(false));
    }

    @Test
    @DisplayName("Should like post successfully")
    void shouldLikePostSuccessfully() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/posts/" + postId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("liked", equalTo(false));

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .put("/api/v1/posts/" + postId + "/likes")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/posts/" + postId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("liked", equalTo(true))
                .body("likeCount", equalTo(1));

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .put("/api/v1/posts/" + postId + "/likes")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/posts/" + postId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("liked", equalTo(false))
                .body("likeCount", equalTo(0));
    }

    @Test
    @DisplayName("Should update post successfully")
    void shouldUpdatePostSuccessfully() {
        Post post = postService.getById(postId);

        UpdatePostDto updatePostDto = UpdatePostDto.builder()
                .caption("Updated caption")
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .body(updatePostDto)
                .when()
                .put("/api/v1/posts/" + postId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(postId.toString()))
                .body("caption", equalTo(updatePostDto.getCaption()))
                .body("updatedAt", not(equalTo(post.getUpdatedAt().toString())));
    }

    @Test
    @DisplayName("Should get post by id")
    void shouldGetPostById() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/posts/" + postId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(postId.toString()))
                .body("user.id", equalTo(testUser.getId().toString()))
                .body("user.username", equalTo(testUser.getUsername()))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue());
    }

    @Test
    @DisplayName("Should get all posts with pagination")
    void shouldGetAllPostsWithPagination() {
        CreatePostDto createPostDto = CreatePostDto.builder()
                .caption("Second test post")
                .imageUrl("https://example.com/second-image.jpg")
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .body(createPostDto)
                .when()
                .post("/api/v1/posts")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/posts?page=0&size=10")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(greaterThanOrEqualTo(2)))
                .body("pageable", notNullValue())
                .body("totalElements", greaterThanOrEqualTo(2))
                .body("totalPages", greaterThan(0))
                .body("content[0].id", notNullValue())
                .body("content[0].user", notNullValue())
                .body("content[0].caption", notNullValue());
    }

    @Test
    @DisplayName("Should filter posts by query parameter")
    void shouldFilterPostsByQuery() {
        String uniqueCaption = "Unique caption " + System.currentTimeMillis();

        CreatePostDto createPostDto = CreatePostDto.builder()
                .caption(uniqueCaption)
                .imageUrl("https://example.com/unique-image.jpg")
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .body(createPostDto)
                .when()
                .post("/api/v1/posts")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/posts?q=" + uniqueCaption)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(greaterThanOrEqualTo(1)))
                .body("content.find { it.caption == '" + uniqueCaption + "' }", notNullValue());
    }

    @Test
    @DisplayName("Should filter posts by username")
    void shouldFilterPostsByUsername() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/posts?user.username=" + testUser.getUsername())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(greaterThanOrEqualTo(1)))
                .body("content[0].user.username", equalTo(testUser.getUsername()));
    }

    @Test
    @DisplayName("Should get post stats")
    void shouldGetPostStats() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/posts/stats")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("totalPosts", greaterThanOrEqualTo(1))
                .body("totalLikes", notNullValue());
    }

    @Test
    @DisplayName("Should get posts by user id")
    void shouldGetPostsByUserId() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/users/" + testUser.getId() + "/posts")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(greaterThanOrEqualTo(1)))
                .body("content[0].user.id", equalTo(testUser.getId().toString()));
    }

    @Test
    @DisplayName("Should delete post successfully")
    void shouldDeletePostSuccessfully() {
        CreatePostDto createPostDto = CreatePostDto.builder()
                .caption("Post to delete")
                .imageUrl("https://example.com/delete-image.jpg")
                .build();

        PostDto postToDelete = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .body(createPostDto)
                .when()
                .post("/api/v1/posts")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(PostDto.class);

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .delete("/api/v1/posts/" + postToDelete.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/posts/" + postToDelete.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", containsString("Post not found"))
                .body("timestamp", notNullValue());
    }

    @Test
    @DisplayName("Should return not found for invalid post id")
    void shouldReturnNotFoundForInvalidPostId() {
        UUID invalidId = UUID.randomUUID();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/v1/posts/" + invalidId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", containsString("Post not found"))
                .body("timestamp", notNullValue());
    }

    @Test
    @DisplayName("Should return unauthorized when accessing without token")
    void shouldReturnUnauthorizedWhenAccessingWithoutToken() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get("/api/v1/posts")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("Should not allow update of post by another user")
    void shouldNotAllowUpdateOfPostByAnotherUser() {
        String otherUsername = "otheruser" + System.currentTimeMillis();
        String otherPassword = "Test@123456";

        CreateAccountDto otherAccountDto = CreateAccountDto.builder()
                .fullName("Other User")
                .username(otherUsername)
                .email(otherUsername + "@example.com")
                .password(otherPassword)
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(otherAccountDto)
                .when()
                .post("/api/v1/account")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        User otherUser = userService.getByEmail(otherAccountDto.getEmail());
        otherUser.setEmailVerified(true);
        userService.save(otherUser);

        UsernamePasswordDto otherCredentials = UsernamePasswordDto.builder()
                .username(otherAccountDto.getEmail())
                .password(otherPassword)
                .build();

        TokenDto otherTokenDto = tokenService.request(otherCredentials);
        String otherAccessToken = otherTokenDto.getAccessToken();

        UpdatePostDto updatePostDto = UpdatePostDto.builder()
                .caption("Unauthorized update")
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + otherAccessToken)
                .body(updatePostDto)
                .when()
                .put("/api/v1/posts/" + postId)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Should not allow deletion of post by another user")
    void shouldNotAllowDeletionOfPostByAnotherUser() {
        String otherUsername = "otheruser2" + System.currentTimeMillis();
        String otherPassword = "Test@123456";

        CreateAccountDto otherAccountDto = CreateAccountDto.builder()
                .fullName("Other User 2")
                .username(otherUsername)
                .email(otherUsername + "@example.com")
                .password(otherPassword)
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(otherAccountDto)
                .when()
                .post("/api/v1/account")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        User otherUser = userService.getByEmail(otherAccountDto.getEmail());
        otherUser.setEmailVerified(true);
        userService.save(otherUser);

        UsernamePasswordDto otherCredentials = UsernamePasswordDto.builder()
                .username(otherAccountDto.getEmail())
                .password(otherPassword)
                .build();

        TokenDto otherTokenDto = tokenService.request(otherCredentials);
        String otherAccessToken = otherTokenDto.getAccessToken();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + otherAccessToken)
                .when()
                .delete("/api/v1/posts/" + postId)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Should return validation error for invalid post data")
    void shouldReturnValidationErrorForInvalidPostData() {
        CreatePostDto invalidPostDto = CreatePostDto.builder()
                .caption(null)
                .imageUrl(null)
                .build();

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .body(invalidPostDto)
                .when()
                .post("/api/v1/posts")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("message", containsString("Validation failed"))
                .body("errors", notNullValue());
    }
}
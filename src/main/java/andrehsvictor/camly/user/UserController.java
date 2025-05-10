package andrehsvictor.camly.user;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andrehsvictor.camly.user.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management operations")
@SecurityRequirement(name = "bearer-jwt")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users with optional filtering", description = "Returns a paginated list of users that can be filtered by query parameters for name, username, or exact username match")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token is required", content = @Content)
    })
    @GetMapping("/api/v1/users")
    public Page<UserDto> getAll(
            @Parameter(description = "Search query for user's name or username") @RequestParam(required = false, name = "q") String query,

            @Parameter(description = "Filter by exact username match") @RequestParam(required = false) String username,

            @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable) {
        return userService.getAllWithFilters(query, username, pageable)
                .map(userService::toDto);
    }

    @Operation(summary = "Get user by ID", description = "Retrieves detailed information about a specific user by their UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token is required", content = @Content)
    })
    @GetMapping("/api/v1/users/{id}")
    public UserDto getById(
            @Parameter(description = "User's UUID", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id) {
        return userService.toDto(userService.getById(id));
    }

    @Operation(summary = "Follow or unfollow a user", description = "Toggles the follow status with the specified user. If already following, this will unfollow the user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Follow status toggled successfully"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token is required", content = @Content),
            @ApiResponse(responseCode = "400", description = "Cannot follow yourself or other validation error", content = @Content)
    })
    @PutMapping("/api/v1/users/{id}/followers")
    public ResponseEntity<Void> follow(
            @Parameter(description = "User's UUID to follow/unfollow", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id) {
        userService.follow(id);
        return ResponseEntity.noContent().build();
    }
}
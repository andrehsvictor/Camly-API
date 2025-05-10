package andrehsvictor.camly.token;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import andrehsvictor.camly.token.dto.IdTokenDto;
import andrehsvictor.camly.token.dto.RefreshTokenDto;
import andrehsvictor.camly.token.dto.RevokeTokenDto;
import andrehsvictor.camly.token.dto.TokenDto;
import andrehsvictor.camly.token.dto.UsernamePasswordDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Token management operations")
public class TokenController {

    private final TokenService tokenService;

    @Operation(summary = "Request authentication token", description = "Authenticates a user with username and password credentials and returns access and refresh tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid request format", content = @Content)
    })
    @PostMapping("/api/v1/token")
    public TokenDto request(@Valid @RequestBody UsernamePasswordDto credentials) {
        return tokenService.request(credentials);
    }

    @Operation(summary = "Refresh token", description = "Issues a new access token using a valid refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid, expired or revoked refresh token", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token format", content = @Content)
    })
    @PostMapping("/api/v1/token/refresh")
    public TokenDto refresh(@Valid @RequestBody RefreshTokenDto refreshTokenDto) {
        return tokenService.refresh(refreshTokenDto);
    }

    @Operation(summary = "Revoke token", description = "Invalidates an access or refresh token, preventing its further use")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Token revoked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid token format", content = @Content)
    })
    @PostMapping("/api/v1/token/revoke")
    public ResponseEntity<Void> revoke(@Valid @RequestBody RevokeTokenDto revokeTokenDto) {
        tokenService.revoke(revokeTokenDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Authenticate with Google", description = "Authenticates a user with Google ID token and returns access and refresh tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Google authentication successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid Google ID token", content = @Content)
    })
    @PostMapping("/api/v1/token/google")
    public TokenDto google(@Valid @RequestBody IdTokenDto idTokenDto) {
        return tokenService.google(idTokenDto);
    }
}
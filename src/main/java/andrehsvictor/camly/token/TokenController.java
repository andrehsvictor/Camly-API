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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/api/v1/token")
    public TokenDto request(@Valid @RequestBody UsernamePasswordDto credentials) {
        return tokenService.request(credentials);
    }

    @PostMapping("/api/v1/token/refresh")
    public TokenDto refresh(@Valid @RequestBody RefreshTokenDto refreshTokenDto) {
        return tokenService.refresh(refreshTokenDto);
    }

    @PostMapping("/api/v1/token/revoke")
    public ResponseEntity<Void> revoke(@Valid @RequestBody RevokeTokenDto revokeTokenDto) {
        tokenService.revoke(revokeTokenDto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/token/google")
    public TokenDto google(@Valid @RequestBody IdTokenDto idTokenDto) {
        return tokenService.google(idTokenDto);
    }

}

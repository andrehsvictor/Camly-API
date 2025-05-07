package andrehsvictor.camly.token;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import andrehsvictor.camly.authentication.AuthenticationService;
import andrehsvictor.camly.jwt.JwtService;
import andrehsvictor.camly.jwt.JwtType;
import andrehsvictor.camly.revokedtoken.RevokedTokenService;
import andrehsvictor.camly.security.UserDetailsImpl;
import andrehsvictor.camly.token.dto.RefreshTokenDto;
import andrehsvictor.camly.token.dto.RevokeTokenDto;
import andrehsvictor.camly.token.dto.TokenDto;
import andrehsvictor.camly.token.dto.UsernamePasswordDto;
import andrehsvictor.camly.user.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RevokedTokenService revokedTokenService;
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public TokenDto request(UsernamePasswordDto credentials) {
        Authentication authentication = authenticationService.authenticate(
                credentials.getUsername(),
                credentials.getPassword());

        User user = ((UserDetailsImpl) authentication.getPrincipal()).getUser();

        Jwt accessToken = jwtService.issue(user, JwtType.ACCESS);
        Jwt refreshToken = jwtService.issue(user, JwtType.REFRESH);

        return buildTokenResponse(accessToken, refreshToken);
    }

    public TokenDto refresh(RefreshTokenDto refreshTokenDto) {
        Jwt oldRefreshToken = jwtService.decode(refreshTokenDto.getRefreshToken());
        
        Jwt accessToken = jwtService.issue(oldRefreshToken, JwtType.ACCESS);
        Jwt newRefreshToken = jwtService.issue(oldRefreshToken, JwtType.REFRESH);
        revokedTokenService.revoke(oldRefreshToken);
        
        return buildTokenResponse(accessToken, newRefreshToken);
    }

    public void revoke(RevokeTokenDto revokeTokenDto) {
        Jwt token = jwtService.decode(revokeTokenDto.getToken());
        revokedTokenService.revoke(token);
    }

    private TokenDto buildTokenResponse(Jwt accessToken, Jwt refreshToken) {
        long expiresIn = accessToken.getExpiresAt().getEpochSecond() - accessToken.getIssuedAt().getEpochSecond();

        return TokenDto.builder()
                .accessToken(accessToken.getTokenValue())
                .refreshToken(refreshToken.getTokenValue())
                .expiresIn(expiresIn)
                .build();
    }
}
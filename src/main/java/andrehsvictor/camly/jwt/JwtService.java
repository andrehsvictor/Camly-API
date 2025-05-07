package andrehsvictor.camly.jwt;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import andrehsvictor.camly.exception.BadRequestException;
import andrehsvictor.camly.exception.UnauthorizedException;
import andrehsvictor.camly.user.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtLifespanProperties jwtLifespanProperties;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public Jwt issue(User user, JwtType type) {
        return switch (type) {
            case ACCESS -> createToken(user, type, jwtLifespanProperties.getAccessTokenLifespan());
            case REFRESH -> createToken(user, type, jwtLifespanProperties.getRefreshTokenLifespan());
            default -> throw new IllegalArgumentException("Unsupported token type: " + type);
        };
    }

    public Jwt issue(Jwt refreshToken, JwtType type) {
        validateRefreshTokenType(refreshToken);
        User user = extractUserFromToken(refreshToken);
        return issue(user, type);
    }

    private void validateRefreshTokenType(Jwt token) {
        String tokenType = token.getClaim("type");
        if (!JwtType.REFRESH.getType().equals(tokenType)) {
            throw new BadRequestException("Expected refresh token, but got: " + tokenType);
        }
    }

    private User extractUserFromToken(Jwt token) {
        return User.builder()
                .id(UUID.fromString(token.getSubject()))
                .username(token.getClaim("username"))
                .email(token.getClaim("email"))
                .emailVerified(token.getClaim("email_verified"))
                .roles(token.getClaim("roles"))
                .build();
    }

    private Jwt createToken(User user, JwtType type, java.time.Duration lifespan) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(lifespan);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getId().toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("type", type.getType())
                .claim("roles", user.getRoles())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("email_verified", user.isEmailVerified())
                .id(UUID.randomUUID().toString())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims));
    }

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            throw new UnauthorizedException("No JWT authentication found in security context");
        }

        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid user ID format in token");
        }
    }

    public Jwt decode(String token) {
        try {
            return jwtDecoder.decode(token);
        } catch (JwtException e) {
            throw new UnauthorizedException(e.getMessage());
        }
    }
}
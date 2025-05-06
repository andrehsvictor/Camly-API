package andrehsvictor.camly.jwt;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import andrehsvictor.camly.exception.UnauthorizedException;
import andrehsvictor.camly.user.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtLifespanProperties jwtLifespanProperties;
    private final JwtEncoder jwtEncoder;
    private final Map<JwtType, BiFunction<User, JwtType, Jwt>> tokenGenerators;

    public JwtService(JwtLifespanProperties jwtLifespanProperties, JwtEncoder jwtEncoder) {
        this.jwtLifespanProperties = jwtLifespanProperties;
        this.jwtEncoder = jwtEncoder;

        this.tokenGenerators = new HashMap<>();
        this.tokenGenerators.put(JwtType.ACCESS, this::issueAccessToken);
        this.tokenGenerators.put(JwtType.REFRESH, this::issueRefreshToken);
    }

    public Jwt issue(User user, JwtType type) {
        return tokenGenerators.getOrDefault(type, (u, t) -> {
            throw new IllegalArgumentException("Invalid token type: " + t);
        }).apply(user, type);
    }

    private Jwt issueAccessToken(User user, JwtType type) {
        Instant iat = Instant.now();
        Instant exp = iat.plus(jwtLifespanProperties.getAccessTokenLifespan());

        JwtClaimsSet claims = getBaseClaimsBuilder(user, iat, exp, type)
                .claim("roles", user.getRoles())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("email_verified", user.isEmailVerified())
                .build();

        return encode(claims);
    }

    private Jwt issueRefreshToken(User user, JwtType type) {
        Instant iat = Instant.now();
        Instant exp = iat.plus(jwtLifespanProperties.getRefreshTokenLifespan());

        JwtClaimsSet claims = getBaseClaimsBuilder(user, iat, exp, type)
                .build();

        return encode(claims);
    }

    private JwtClaimsSet.Builder getBaseClaimsBuilder(User user, Instant issuedAt, Instant expiresAt, JwtType type) {
        return JwtClaimsSet.builder()
                .subject(user.getId().toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("type", type.getType())
                .id(UUID.randomUUID().toString());
    }

    private Jwt encode(JwtClaimsSet claims) {
        return jwtEncoder.encode(JwtEncoderParameters.from(claims));
    }

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new UnauthorizedException("No authentication found in security context");
        }
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
        Jwt jwt = jwtAuthenticationToken.getToken();
        String subject = jwt.getSubject();
        return UUID.fromString(subject);
    }
}
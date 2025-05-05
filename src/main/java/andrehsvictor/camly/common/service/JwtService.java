package andrehsvictor.camly.common.service;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import andrehsvictor.camly.common.exception.UnauthorizedException;
import andrehsvictor.camly.common.security.UserDetailsImpl;

@Service
@Validated
public class JwtService {

    public UUID getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return extractUserIdFromJwt(jwtAuth);
        } else if (authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }

        throw new UnauthorizedException("Unsupported authentication type");
    }

    private UUID extractUserIdFromJwt(JwtAuthenticationToken jwtAuth) {
        String userId = jwtAuth.getToken().getClaimAsString("sub");
        if (userId == null) {
            throw new UnauthorizedException("User ID not found in JWT");
        }
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid User ID format in JWT");
        }
    }

}

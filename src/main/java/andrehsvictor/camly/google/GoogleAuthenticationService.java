package andrehsvictor.camly.google;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import andrehsvictor.camly.exception.BadRequestException;
import andrehsvictor.camly.exception.ResourceConflictException;
import andrehsvictor.camly.exception.ResourceNotFoundException;
import andrehsvictor.camly.security.UserDetailsImpl;
import andrehsvictor.camly.user.Role;
import andrehsvictor.camly.user.User;
import andrehsvictor.camly.user.UserProvider;
import andrehsvictor.camly.user.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleAuthenticationService {

    private final GoogleIdTokenVerifier tokenVerifier;
    private final UserService userService;

    public Authentication authenticate(String idToken) {
        GoogleIdToken.Payload payload = verifyIdToken(idToken);
        User user = findOrRegisterUser(payload);
        return createAuthentication(user);
    }

    private GoogleIdToken.Payload verifyIdToken(String idToken) {
        try {
            GoogleIdToken googleIdToken = tokenVerifier.verify(idToken);
            if (googleIdToken == null) {
                throw new BadRequestException("Invalid ID token");
            }
            return googleIdToken.getPayload();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to verify ID token", e);
        }
    }

    private User findOrRegisterUser(GoogleIdToken.Payload payload) {
        String email = payload.getEmail();
        String providerId = payload.getSubject();
        boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
        String pictureUrl = (String) payload.get("picture");
        String name = (String) payload.get("name");

        User user = findExistingUser(email, providerId);

        if (user == null) {
            return createGoogleUser(email, providerId, emailVerified, name, pictureUrl);
        }

        return updateUserIfNeeded(user, emailVerified, pictureUrl, providerId);
    }

    private User findExistingUser(String email, String providerId) {
        try {
            User user = userService.getByProviderId(providerId);

            if (!email.equals(user.getEmail())) {
                throw new ResourceConflictException(
                        "User with provider ID '" + providerId + "' has a different email address");
            }

            return user;
        } catch (ResourceNotFoundException e) {
            try {
                User user = userService.getByEmail(email);

                if (user.getProvider() != UserProvider.GOOGLE) {
                    throw new ResourceConflictException(
                            "User with email '" + email + "' is already registered with a different provider");
                }

                return user;
            } catch (ResourceNotFoundException ex) {
                return null;
            }
        }
    }

    private User updateUserIfNeeded(User user, boolean emailVerified, String pictureUrl, String providerId) {
        boolean needsUpdate = false;

        if (user.getProviderId() == null) {
            user.setProviderId(providerId);
            needsUpdate = true;
        }

        if (!user.isEmailVerified() && emailVerified) {
            user.setEmailVerified(emailVerified);
            needsUpdate = true;
        }

        if (user.getPictureUrl() == null && pictureUrl != null) {
            user.setPictureUrl(pictureUrl);
            needsUpdate = true;
        }

        return needsUpdate ? userService.save(user) : user;
    }

    private User createGoogleUser(String email, String providerId, boolean emailVerified, String name,
            String pictureUrl) {
        User newUser = User.builder()
                .email(email)
                .username(generateUniqueUsername(email))
                .fullName(name)
                .emailVerified(emailVerified)
                .pictureUrl(pictureUrl)
                .provider(UserProvider.GOOGLE)
                .providerId(providerId)
                .role(Role.USER)
                .build();

        return userService.save(newUser);
    }

    private String generateUniqueUsername(String email) {
        String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();

        if (!userService.existsByUsername(baseUsername)) {
            return baseUsername;
        }

        String username;
        do {
            String suffix = UUID.randomUUID().toString().substring(0, 6);
            username = baseUsername + suffix;
        } while (userService.existsByUsername(username));

        return username;
    }

    private Authentication createAuthentication(User user) {
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        var authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().name()));

        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }
}
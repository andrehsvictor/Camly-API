package andrehsvictor.camly.google;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import andrehsvictor.camly.exception.BadRequestException;
import andrehsvictor.camly.exception.ResourceConflictException;
import andrehsvictor.camly.exception.UnauthorizedException;
import andrehsvictor.camly.security.UserDetailsImpl;
import andrehsvictor.camly.user.Role;
import andrehsvictor.camly.user.User;
import andrehsvictor.camly.user.UserProvider;
import andrehsvictor.camly.user.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleAuthenticationService {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final UserService userService;

    public Authentication authenticate(String idToken) {
        try {
            GoogleIdToken googleIdToken = googleIdTokenVerifier.verify(idToken);
            if (googleIdToken == null) {
                throw new BadRequestException("Failed to verify Google ID token");
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            User user = findOrRegisterUser(payload);

            return createAuthentication(user);
        } catch (GeneralSecurityException | IOException e) {
            throw new UnauthorizedException("Failed to verify Google ID token");
        }
    }

    private User findOrRegisterUser(GoogleIdToken.Payload payload) {
        String email = payload.getEmail();
        boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
        String pictureUrl = (String) payload.get("picture");
        String name = (String) payload.get("name");

        try {
            User user = userService.getByEmail(email);

            if (user.getProvider() != UserProvider.GOOGLE) {
                throw new ResourceConflictException(
                        "User with email '" + email + "'' is already registered with a different provider");
            }

            if (updateUserIfNeeded(user, emailVerified, pictureUrl)) {
                userService.save(user);
            }

            return user;
        } catch (Exception e) {
            return createGoogleUser(email, emailVerified, name, pictureUrl);
        }
    }

    private boolean updateUserIfNeeded(User user, boolean emailVerified, String pictureUrl) {
        boolean needsUpdate = false;

        if (!user.isEmailVerified() && emailVerified) {
            user.setEmailVerified(emailVerified);
            needsUpdate = true;
        }

        if (user.getPictureUrl() == null && pictureUrl != null) {
            user.setPictureUrl(pictureUrl);
            needsUpdate = true;
        }

        return needsUpdate;
    }

    private User createGoogleUser(String email, boolean emailVerified, String name, String pictureUrl) {
        String username = generateUniqueUsername(email);

        User newUser = User.builder()
                .email(email)
                .username(username)
                .fullName(name)
                .emailVerified(emailVerified)
                .pictureUrl(pictureUrl)
                .provider(UserProvider.GOOGLE)
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
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().name()));

        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }
}
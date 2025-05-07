package andrehsvictor.camly.authentication;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import andrehsvictor.camly.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;

    public Authentication authenticate(String username, String password) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);
        try {
            return authenticationManager.authenticate(authentication);
        } catch (DisabledException e) {
            throw new UnauthorizedException("Your email is not verified yet");
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid credentials");
        } catch (Exception e) {
            throw new UnauthorizedException("Authentication failed");
        }
    }
}

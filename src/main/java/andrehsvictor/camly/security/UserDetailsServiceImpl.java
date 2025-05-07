package andrehsvictor.camly.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import andrehsvictor.camly.user.User;
import andrehsvictor.camly.user.UserProvider;
import andrehsvictor.camly.user.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmailAndProvider(username, UserProvider.LOCAL)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username));
        return new UserDetailsImpl(user);
    }

}

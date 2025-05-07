package andrehsvictor.camly.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("""
            SELECT u FROM User u
            WHERE (u.username = ?1 OR u.email = ?1)
            AND u.provider = ?2
                """)
    Optional<User> findByUsernameOrEmailAndProvider(String usernameOrEmail, UserProvider provider);

    Optional<User> findByEmailVerificationToken(String token);

    Optional<User> findByResetPasswordToken(String token);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

}

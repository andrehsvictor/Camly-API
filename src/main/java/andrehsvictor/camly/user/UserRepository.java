package andrehsvictor.camly.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("""
            SELECT u FROM User u
            WHERE (u.username = ?1 OR u.email = ?1)
            AND u.provider = ?2
                """)
    Optional<User> findByUsernameOrEmailAndProvider(String usernameOrEmail, UserProvider provider);

    Optional<User> findByProviderId(String providerId);

    Optional<User> findByEmailVerificationToken(String token);

    Optional<User> findByResetPasswordToken(String token);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("""
            SELECT u FROM User u
            WHERE (
                LOWER(:query) IS NULL
                OR LOWER(u.username) LIKE CONCAT('%', LOWER(:query), '%')
                OR LOWER(u.fullName) LIKE CONCAT('%', LOWER(:query), '%')
            )
            AND (
                :username IS NULL
                OR u.username = :username
            )
            """)
    Page<User> findAllWithFilters(
            String query,
            String username,
            Pageable pageable);

    Page<User> findAllFollowersByUserId(UUID userId, Pageable pageable);

    Page<User> findAllFollowingByUserId(UUID userId, Pageable pageable);

}

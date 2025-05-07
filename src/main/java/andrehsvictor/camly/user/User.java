package andrehsvictor.camly.user;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "id", "username", "email" })
@ToString(exclude = { "password", "emailVerificationToken", "resetPasswordToken" })
public class User implements Serializable {

    private static final long serialVersionUID = -8607404488005893145L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Builder.Default
    @Column(name = "provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserProvider provider = UserProvider.LOCAL;

    @Column(name = "provider_id", unique = true)
    private String providerId;

    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "picture_url")
    private String pictureUrl;

    @Builder.Default
    @Column(name = "post_count", nullable = false)
    private Integer postCount = 0;

    @Builder.Default
    @Column(name = "follower_count", nullable = false)
    private Integer followerCount = 0;

    @Builder.Default
    @Column(name = "following_count", nullable = false)
    private Integer followingCount = 0;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "bio")
    private String bio;

    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "email_verification_token_expires_at")
    private LocalDateTime emailVerificationTokenExpiresAt;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "reset_password_token_expires_at")
    private LocalDateTime resetPasswordTokenExpiresAt;

    @Builder.Default
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
package andrehsvictor.camly.account.dto;

import java.util.List;

import lombok.Data;

@Data
public class AccountDto {

    private String id;
    private String fullName;
    private String username;
    private String email;
    private String pictureUrl;
    private String bio;
    private String provider;
    private Integer postCount;
    private Integer followerCount;
    private Integer followingCount;
    private boolean emailVerified;
    private List<String> roles;
    private String createdAt;
    private String updatedAt;
    
}

/*
 * public class User implements Serializable {
 * 
 * private static final long serialVersionUID = -8607404488005893145L;
 * 
 * @Id
 * 
 * @GeneratedValue(strategy = GenerationType.UUID)
 * 
 * @Column(name = "id", updatable = false, nullable = false)
 * private UUID id;
 * 
 * @Column(name = "username", unique = true, nullable = false)
 * private String username;
 * 
 * @Column(name = "password")
 * private String password;
 * 
 * @Column(name = "email", unique = true, nullable = false)
 * private String email;
 * 
 * @Builder.Default
 * 
 * @Column(name = "provider", nullable = false)
 * 
 * @Enumerated(EnumType.STRING)
 * private UserProvider provider = UserProvider.LOCAL;
 * 
 * @Builder.Default
 * 
 * @Column(name = "email_verified", nullable = false)
 * private boolean emailVerified = false;
 * 
 * @Column(name = "picture_url")
 * private String pictureUrl;
 * 
 * @Builder.Default
 * 
 * @Column(name = "post_count", nullable = false)
 * private Integer postCount = 0;
 * 
 * @Builder.Default
 * 
 * @Column(name = "follower_count", nullable = false)
 * private Integer followerCount = 0;
 * 
 * @Builder.Default
 * 
 * @Column(name = "following_count", nullable = false)
 * private Integer followingCount = 0;
 * 
 * @Column(name = "full_name", nullable = false)
 * private String fullName;
 * 
 * @Column(name = "bio")
 * private String bio;
 * 
 * @Column(name = "email_verification_token")
 * private String emailVerificationToken;
 * 
 * @Column(name = "email_verification_token_expires_at")
 * private LocalDateTime emailVerificationTokenExpiresAt;
 * 
 * @Column(name = "reset_password_token")
 * private String resetPasswordToken;
 * 
 * @Column(name = "reset_password_token_expires_at")
 * private LocalDateTime resetPasswordTokenExpiresAt;
 * 
 * @Builder.Default
 * 
 * @Column(name = "created_at", updatable = false, nullable = false)
 * private LocalDateTime createdAt = LocalDateTime.now();
 * 
 * @Builder.Default
 * 
 * @Column(name = "updated_at", nullable = false)
 * private LocalDateTime updatedAt = LocalDateTime.now();
 * 
 * @ElementCollection
 * 
 * @CollectionTable(name = "users_roles", joinColumns = @JoinColumn(name =
 * "user_id"))
 * 
 * @Column(name = "name")
 * 
 * @Builder.Default
 * private Set<String> roles = new HashSet<>();
 */
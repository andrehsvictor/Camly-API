package andrehsvictor.camly.account.dto;

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
    private String role;
    private String createdAt;
    private String updatedAt;

}
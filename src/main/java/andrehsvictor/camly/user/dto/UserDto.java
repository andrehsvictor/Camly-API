package andrehsvictor.camly.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private String id;
    private String username;
    private String pictureUrl;
    private String fullName;
    private String bio;
    private Integer followerCount;
    private Integer followingCount;
    private Integer postCount;
    private String createdAt;
}

package andrehsvictor.camly.post.dto;

import andrehsvictor.camly.user.dto.UserDto;
import lombok.Data;

@Data
public class PostDto {

    private String id;
    private String imageUrl;
    private String caption;
    private Boolean liked;
    private UserDto user;
    private Integer likeCount;
    private String createdAt;
    private String updatedAt;

}

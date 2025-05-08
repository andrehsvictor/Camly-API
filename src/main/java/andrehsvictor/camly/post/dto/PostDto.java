package andrehsvictor.camly.post.dto;

import lombok.Data;

@Data
public class PostDto {

    private String id;
    private String imageUrl;
    private String caption;
    private Boolean liked;
    private Integer likeCount;

}

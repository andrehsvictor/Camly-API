package andrehsvictor.camly.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePostDto {

    @NotBlank(message = "Caption is required")
    @Size(max = 255, message = "Caption must be at most 255 characters")
    private String caption;

    @NotBlank(message = "Image URL is required")
    @Pattern(regexp = "^(https?://.*\\.(?:png|jpg|jpeg))$", message = "Image URL must be a valid URL ending with .png, .jpg, or .jpeg")
    @Size(max = 255, message = "Image URL must be at most 255 characters")
    private String imageUrl;

}

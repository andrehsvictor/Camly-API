package andrehsvictor.camly.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdatePostDto {

    @NotBlank(message = "Caption is required")
    @Size(max = 255, message = "Caption must be at most 255 characters")
    private String caption;

}

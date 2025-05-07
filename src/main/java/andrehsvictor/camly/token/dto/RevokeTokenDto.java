package andrehsvictor.camly.token.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RevokeTokenDto {

    @NotBlank(message = "Token is required")
    @Pattern(message = "Token should be a valid JWT", regexp = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_.+/=]*$")
    private String token;

}

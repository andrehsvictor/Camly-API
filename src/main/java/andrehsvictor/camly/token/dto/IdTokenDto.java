package andrehsvictor.camly.token.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IdTokenDto {

    @NotBlank(message = "idToken must not be blank")
    @Pattern(regexp = "^[a-zA-Z0-9\\-\\._~\\+\\/]+=*$", message = "idToken must be a valid JWT token")
    private String idToken;
    
}

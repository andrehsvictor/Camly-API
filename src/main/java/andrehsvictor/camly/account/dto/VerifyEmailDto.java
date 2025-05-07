package andrehsvictor.camly.account.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyEmailDto {

    @NotBlank(message = "Token is required")
    private String token;
    
}

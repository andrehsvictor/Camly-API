package andrehsvictor.camly.account.dto;

import andrehsvictor.camly.account.EmailSendingAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendActionEmailDto {

    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[\\w-\\.]+@[\\w-]+\\.[a-zA-Z]{2,}$", message = "Email is not valid")
    private String email;

    @NotNull(message = "Action is required")
    private EmailSendingAction action;

    @NotBlank(message = "URL is required")
    @Pattern(regexp = "^(https?://)?(www\\.)?[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}(/\\S*)?$", message = "URL is not valid")
    @Size(max = 255, message = "URL must be less than 255 characters")
    private String url;

}

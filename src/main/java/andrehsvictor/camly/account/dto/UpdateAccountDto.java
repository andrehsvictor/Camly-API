package andrehsvictor.camly.account.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateAccountDto {

    @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
    private String fullName;

    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @Pattern(message = "Username can only contain lowercase letters, numbers, and underscores", regexp = "^[a-z0-9_]+$")
    private String username;

    @Pattern(regexp = "^[\\w-\\.]+@[\\w-]+\\.[a-zA-Z]{2,}$", message = "Email is not valid")
    private String email;

    @Pattern(regexp = "^(https?://)?(www\\.)?[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}(/\\S*)?$", message = "Picture URL is not valid")
    @Size(max = 255, message = "Picture URL must be less than 255 characters")
    private String pictureUrl;

    @Size(max = 255, message = "Bio must be less than 255 characters")
    private String bio;

}
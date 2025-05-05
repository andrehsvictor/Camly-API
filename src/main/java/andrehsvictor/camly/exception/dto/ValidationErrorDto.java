package andrehsvictor.camly.exception.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidationErrorDto {
    private int status;
    private String message;
    private String requestId;
    private LocalDateTime timestamp;
    private List<FieldErrorDto> errors;

    @Data
    @Builder
    public static class FieldErrorDto {
        private String field;
        private String message;
    }
}

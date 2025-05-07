package andrehsvictor.camly.exception.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FieldErrorDto {
        private String field;
        private String message;
    }
}

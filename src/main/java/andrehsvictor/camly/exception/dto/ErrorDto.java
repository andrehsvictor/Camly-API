package andrehsvictor.camly.exception.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorDto {

    private int status;
    private String message;
    private String requestId;
    private LocalDateTime timestamp;

}
package andrehsvictor.camly.common.exception.handler;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import andrehsvictor.camly.common.exception.BadRequestException;
import andrehsvictor.camly.common.exception.ForbiddenOperationException;
import andrehsvictor.camly.common.exception.ResourceConflictException;
import andrehsvictor.camly.common.exception.UnauthorizedException;
import andrehsvictor.camly.common.exception.dto.ErrorDto;
import andrehsvictor.camly.common.exception.dto.ValidationErrorDto;
import andrehsvictor.camly.common.exception.dto.ValidationErrorDto.FieldErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> FieldErrorDto.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        var errorDto = ValidationErrorDto.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .requestId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .errors(fieldErrors)
                .build();

        log.error("Validation error: {}", ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        var errorDto = ErrorDto.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Missing required parameter: " + ex.getParameterName())
                .requestId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build();

        log.error("Missing parameter: {}", ex.getParameterName());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDto> handleBadRequestException(
            BadRequestException ex,
            HttpServletRequest request) {

        var errorDto = ErrorDto.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .requestId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build();

        log.error("Bad request: {}", ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ErrorDto> handleForbiddenOperationException(
            ForbiddenOperationException ex,
            HttpServletRequest request) {

        var errorDto = ErrorDto.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message(ex.getMessage())
                .requestId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build();

        log.error("Forbidden operation: {}", ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ErrorDto> handleResourceConflictException(
            ResourceConflictException ex,
            HttpServletRequest request) {

        var errorDto = ErrorDto.builder()
                .status(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .requestId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build();

        log.error("Resource conflict: {}", ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorDto> handleUnauthorizedException(
            UnauthorizedException ex,
            HttpServletRequest request) {

        var errorDto = ErrorDto.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message(ex.getMessage())
                .requestId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build();

        log.error("Unauthorized access: {}", ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({ BadCredentialsException.class, AuthenticationException.class })
    public ResponseEntity<ErrorDto> handleAuthenticationException(
            Exception ex,
            HttpServletRequest request) {

        var errorDto = ErrorDto.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message(ex.getMessage())
                .requestId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build();

        log.error("Authentication failed: {}", ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorDto> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request) {

        var errorDto = ErrorDto.builder()
                .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
                .message("File upload size exceeded the maximum allowed")
                .requestId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build();

        log.error("File upload too large: {}", ex.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        var errorDto = ErrorDto.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred")
                .requestId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build();

        log.error("Unexpected error: ", ex);
        return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
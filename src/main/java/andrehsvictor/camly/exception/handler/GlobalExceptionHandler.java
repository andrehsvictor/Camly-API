package andrehsvictor.camly.exception.handler;

import java.time.LocalDateTime;
import java.util.List;
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

import andrehsvictor.camly.exception.BadRequestException;
import andrehsvictor.camly.exception.ForbiddenOperationException;
import andrehsvictor.camly.exception.ResourceConflictException;
import andrehsvictor.camly.exception.ResourceNotFoundException;
import andrehsvictor.camly.exception.UnauthorizedException;
import andrehsvictor.camly.exception.dto.ErrorDto;
import andrehsvictor.camly.exception.dto.ValidationErrorDto;
import andrehsvictor.camly.exception.dto.ValidationErrorDto.FieldErrorDto;
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

        List<FieldErrorDto> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDto(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        ValidationErrorDto errorDto = ValidationErrorDto.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .requestId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .errors(fieldErrors)
                .build();

        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ErrorDto errorDto = createErrorDto(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "File upload size exceeded the maximum allowed");
        return new ResponseEntity<>(errorDto, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ErrorDto errorDto = createErrorDto(
                HttpStatus.BAD_REQUEST,
                "Missing required parameter: " + ex.getParameterName());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDto> handleBadRequestException(BadRequestException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ErrorDto> handleForbiddenOperationException(ForbiddenOperationException ex) {
        return createErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ErrorDto> handleResourceConflictException(ResourceConflictException ex) {
        return createErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorDto> handleUnauthorizedException(UnauthorizedException ex) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDto> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({ BadCredentialsException.class, AuthenticationException.class })
    public ResponseEntity<ErrorDto> handleAuthenticationException(Exception ex) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleGenericException(Exception ex) {
        return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred");
    }

    private ResponseEntity<ErrorDto> createErrorResponse(HttpStatus status, String message) {
        ErrorDto errorDto = createErrorDto(status, message);
        return new ResponseEntity<>(errorDto, status);
    }

    private ErrorDto createErrorDto(HttpStatus status, String message) {
        return ErrorDto.builder()
                .status(status.value())
                .message(message)
                .requestId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
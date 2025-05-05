package andrehsvictor.camly.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    private static final long serialVersionUID = -4284782755981912757L;

    public BadRequestException(String message) {
        super(message);
    }

}

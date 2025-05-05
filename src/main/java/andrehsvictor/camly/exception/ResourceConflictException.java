package andrehsvictor.camly.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class ResourceConflictException extends RuntimeException {

    private static final long serialVersionUID = -7758286956645663292L;

    public ResourceConflictException(String message) {
        super(message);
    }

}

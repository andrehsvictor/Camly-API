package andrehsvictor.camly.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN)
public class ForbiddenOperationException extends RuntimeException {

    private static final long serialVersionUID = -1483167243074167619L;

    public ForbiddenOperationException(String message) {
        super(message);
    }

}

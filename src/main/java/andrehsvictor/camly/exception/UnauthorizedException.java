package andrehsvictor.camly.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {

    private static final long serialVersionUID = -6052480707108787323L;

    public UnauthorizedException(String message) {
        super(message);
    }

}

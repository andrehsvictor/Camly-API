package andrehsvictor.camly.exception;

public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -5739422282309497249L;

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s not found with %s: '%s'", resource, field, value));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }

}

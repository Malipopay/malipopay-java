package tz.co.malipopay.exceptions;

/**
 * Thrown when the API returns a 404 Not Found response.
 */
public class NotFoundException extends MalipopayException {

    public NotFoundException(String message) {
        super(message, 404, "NOT_FOUND", null);
    }

    public NotFoundException(String message, String details) {
        super(message, 404, "NOT_FOUND", details);
    }
}

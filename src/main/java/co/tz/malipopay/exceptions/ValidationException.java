package co.tz.malipopay.exceptions;

/**
 * Thrown when the API returns a 422 Unprocessable Entity response.
 */
public class ValidationException extends MalipopayException {

    public ValidationException(String message) {
        super(message, 422, "VALIDATION_ERROR", null);
    }

    public ValidationException(String message, String details) {
        super(message, 422, "VALIDATION_ERROR", details);
    }
}

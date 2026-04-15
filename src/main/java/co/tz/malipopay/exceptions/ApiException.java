package co.tz.malipopay.exceptions;

/**
 * Thrown when the API returns a 5xx server error response.
 */
public class ApiException extends MalipopayException {

    public ApiException(String message) {
        super(message, 500, "API_ERROR", null);
    }

    public ApiException(String message, int statusCode, String details) {
        super(message, statusCode, "API_ERROR", details);
    }
}

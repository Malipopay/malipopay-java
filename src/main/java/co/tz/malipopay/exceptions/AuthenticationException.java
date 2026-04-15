package co.tz.malipopay.exceptions;

/**
 * Thrown when the API returns a 401 Unauthorized response.
 */
public class AuthenticationException extends MalipopayException {

    public AuthenticationException(String message) {
        super(message, 401, "AUTHENTICATION_ERROR", null);
    }

    public AuthenticationException(String message, String details) {
        super(message, 401, "AUTHENTICATION_ERROR", details);
    }
}

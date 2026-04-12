package co.tz.malipopay.exceptions;

/**
 * Thrown when the API returns a 429 Too Many Requests response.
 */
public class RateLimitException extends MaliPoPayException {

    public RateLimitException(String message) {
        super(message, 429, "RATE_LIMIT_ERROR", null);
    }

    public RateLimitException(String message, String details) {
        super(message, 429, "RATE_LIMIT_ERROR", details);
    }
}

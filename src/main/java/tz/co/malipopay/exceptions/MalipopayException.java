package tz.co.malipopay.exceptions;

/**
 * Base exception for all Malipopay SDK errors.
 */
public class MalipopayException extends RuntimeException {

    private final int statusCode;
    private final String code;
    private final String details;

    public MalipopayException(String message) {
        this(message, 0, null, null);
    }

    public MalipopayException(String message, int statusCode, String code, String details) {
        super(message);
        this.statusCode = statusCode;
        this.code = code;
        this.details = details;
    }

    public MalipopayException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.code = null;
        this.details = null;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getCode() {
        return code;
    }

    public String getDetails() {
        return details;
    }
}

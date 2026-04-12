package co.tz.malipopay.exceptions;

/**
 * Base exception for all MaliPoPay SDK errors.
 */
public class MaliPoPayException extends RuntimeException {

    private final int statusCode;
    private final String code;
    private final String details;

    public MaliPoPayException(String message) {
        this(message, 0, null, null);
    }

    public MaliPoPayException(String message, int statusCode, String code, String details) {
        super(message);
        this.statusCode = statusCode;
        this.code = code;
        this.details = details;
    }

    public MaliPoPayException(String message, Throwable cause) {
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

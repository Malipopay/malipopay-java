package co.tz.malipopay.exceptions;

/**
 * Thrown when a network or connection error occurs.
 */
public class ConnectionException extends MaliPoPayException {

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}

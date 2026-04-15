package co.tz.malipopay.exceptions;

/**
 * Thrown when a network or connection error occurs.
 */
public class ConnectionException extends MalipopayException {

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}

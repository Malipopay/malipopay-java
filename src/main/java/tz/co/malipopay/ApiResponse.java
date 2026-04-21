package tz.co.malipopay;

/**
 * Generic API response wrapper returned by all SDK methods.
 *
 * @param <T> the type of the data payload
 */
public class ApiResponse<T> {

    private final boolean success;
    private final int code;
    private final String message;
    private final T data;

    public ApiResponse(boolean success, int code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}

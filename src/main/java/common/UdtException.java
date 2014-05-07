package common;

/**
 * Exception class.
 * @author Shaofeng Chen
 * @since 4/23/14
 */
public class UdtException extends RuntimeException {
    public UdtException(String message) {
        super(message);
    }

    public UdtException(String message, Throwable cause) {
        super(message, cause);
    }

    public UdtException(Throwable cause) {
        super(cause);
    }
}

package io.solwind.exception;

/**
 * Created by theso on 7/3/2017.
 */
public class DedicatedRuntimeException extends RuntimeException {
    public DedicatedRuntimeException(Throwable cause) {
        super(cause);
    }

    public DedicatedRuntimeException(String message) {
        super(message);
    }
}

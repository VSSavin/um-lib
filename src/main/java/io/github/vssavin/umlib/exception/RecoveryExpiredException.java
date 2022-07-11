package io.github.vssavin.umlib.exception;

/**
 * Created by vssavin on 11.07.2022.
 */
public class RecoveryExpiredException extends RuntimeException{
    public RecoveryExpiredException(String message) {
        super(message);
    }

    public RecoveryExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}

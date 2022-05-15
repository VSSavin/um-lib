package io.github.vssavin.umlib.exception;

/**
 * @author vssavin on 12.01.22
 */
public class UserConfirmFailedException extends RuntimeException {

    public UserConfirmFailedException(String message) {
        super(message);
    }
    public UserConfirmFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

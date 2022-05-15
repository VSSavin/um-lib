package io.github.vssavin.umlib.exception;

/**
 * @author vssavin on 23.12.21
 */
public class UserExistsException extends RuntimeException {

    public UserExistsException(String message) {
        super(message);
    }

    public UserExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

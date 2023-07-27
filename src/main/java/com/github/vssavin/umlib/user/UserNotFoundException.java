package com.github.vssavin.umlib.user;

/**
 * Special unchecked exception type used to indicate that the specified user not found.
 *
 * @author vssavin on 20.07.2023
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

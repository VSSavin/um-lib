package com.github.vssavin.umlib.user;

/**
 * Special unchecked exception type used to indicate that an error has occurred in a user service.
 *
 * @author vssavin on 15.07.2023
 */
public class UserServiceException extends RuntimeException {

    public UserServiceException(String msg, Throwable e) {
        super(msg, e);
    }

    public UserServiceException(String message) {
        super(message);
    }
}

package com.github.vssavin.umlib.domain.email;

/**
 * Special unchecked exception type used
 * to indicate that email is not found.
 *
 * @author vssavin on 08.01.2022
 */
public class EmailNotFoundException extends RuntimeException {

    public EmailNotFoundException(String message) {
        super(message);
    }

    public EmailNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

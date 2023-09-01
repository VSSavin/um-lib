package com.github.vssavin.umlib.domain.event;

/**
 * Special unchecked exception type used to indicate that an error occurred while creating an event.
 *
 * @author vssavin on 25.08.2023
 */
public class CreateEventException extends RuntimeException {
    public CreateEventException(String message) {
        super(message);
    }

    public CreateEventException(String message, Throwable cause) {
        super(message, cause);
    }
}

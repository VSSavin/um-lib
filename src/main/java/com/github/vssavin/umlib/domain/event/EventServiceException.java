package com.github.vssavin.umlib.domain.event;

/**
 * Special unchecked exception type used to indicate that an error has occurred in an
 * event service.
 *
 * @author vssavin on 31.08.2023
 */
public class EventServiceException extends RuntimeException {

	public EventServiceException(String message) {
		super(message);
	}

	public EventServiceException(String message, Throwable cause) {
		super(message, cause);
	}

}

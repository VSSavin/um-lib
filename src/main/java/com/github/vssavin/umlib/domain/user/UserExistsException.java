package com.github.vssavin.umlib.domain.user;

/**
 * Special unchecked exception type used to indicate that the specified user exists.
 *
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

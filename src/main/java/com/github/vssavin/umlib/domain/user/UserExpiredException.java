package com.github.vssavin.umlib.domain.user;

/**
 * Special unchecked exception type used to indicate that the specified user has been
 * expired.
 *
 * @author vssavin on 29.08.2023
 */
public class UserExpiredException extends RuntimeException {

	public UserExpiredException(String message) {
		super(message);
	}

}

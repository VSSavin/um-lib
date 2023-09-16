package com.github.vssavin.umlib.domain.user;

/**
 * Special unchecked exception type used to indicate that password recovery failed.
 *
 * @author vssavin on 11.07.2022.
 */
class RecoveryExpiredException extends RuntimeException {

	RecoveryExpiredException(String message) {
		super(message);
	}

	RecoveryExpiredException(String message, Throwable cause) {
		super(message, cause);
	}

}

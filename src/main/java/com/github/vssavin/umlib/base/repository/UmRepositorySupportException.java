package com.github.vssavin.umlib.base.repository;

/**
 * Special unchecked exception type used to indicate that something went wrong during the
 * execution of UmRepositorySupport.
 *
 * @author vssavin on 02.09.2023
 */
public class UmRepositorySupportException extends RuntimeException {

	public UmRepositorySupportException(String message) {
		super(message);
	}

	public UmRepositorySupportException(String message, Throwable cause) {
		super(message, cause);
	}

}

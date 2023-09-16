package com.github.vssavin.umlib.domain.auth;

/**
 * Special unchecked exception type used to indicate that authentication procedure is
 * forbidden.
 *
 * @author vssavin on 29.08.2023
 */
public class AuthenticationForbiddenException extends RuntimeException {

    public AuthenticationForbiddenException(String message) {
        super(message);
    }

}

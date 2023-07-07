package com.github.vssavin.umlib.user;

/**
 * Created by vssavin on 11.07.2022.
 */
class RecoveryExpiredException extends RuntimeException {
    RecoveryExpiredException(String message) {
        super(message);
    }

    RecoveryExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}

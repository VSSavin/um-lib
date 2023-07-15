package com.github.vssavin.umlib.user;

/**
 * @author vssavin on 15.07.2023
 */
public class UserServiceException extends RuntimeException {

    public UserServiceException(String msg, Throwable e) {
        super(msg, e);
    }
}

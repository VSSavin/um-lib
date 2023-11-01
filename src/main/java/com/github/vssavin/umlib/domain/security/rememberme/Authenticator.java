package com.github.vssavin.umlib.domain.security.rememberme;

import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Represents methods for handling authentication.
 *
 * @author vssavin on 01.11.2023
 */
public interface Authenticator {

    Authentication retrieveAuthentication(HttpServletRequest request, HttpServletResponse response);

}

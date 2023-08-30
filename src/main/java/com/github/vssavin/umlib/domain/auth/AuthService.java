package com.github.vssavin.umlib.domain.auth;

import com.github.vssavin.umlib.domain.event.EventType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * Main interface for service provides user authentication.
 *
 * @author vssavin on 29.08.2023
 */
public interface AuthService {
    Authentication authenticate(Authentication authentication);
    Collection<GrantedAuthority> processSuccessAuthentication(Authentication authentication, HttpServletRequest request,
                                                              EventType eventType);
    boolean isAuthenticationAllowed(String ipAddress);
    void processFailureAuthentication(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception);
    Class<? extends Authentication> authenticationClass();
}

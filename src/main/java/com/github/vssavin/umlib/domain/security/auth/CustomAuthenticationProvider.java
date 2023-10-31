package com.github.vssavin.umlib.domain.security.auth;

import com.github.vssavin.umlib.domain.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * An {@link org.springframework.security.authentication.AuthenticationProvider}
 * implementation that attempts to authenticate using corresponding authentication
 * service.
 *
 * @author vssavin on 18.12.2021
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final AuthService authService;

    @Autowired
    public CustomAuthenticationProvider(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return authService.authenticate(authentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(authService.authenticationClass());
    }

}

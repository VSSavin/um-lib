package com.github.vssavin.umlib.security.spring;

import io.github.vssavin.securelib.Utils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Created by vssavin on 01.08.2022.
 */
class CustomUsernamePasswordAuthenticationToken extends UsernamePasswordAuthenticationToken {

    CustomUsernamePasswordAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    CustomUsernamePasswordAuthenticationToken(Object principal, Object credentials,
                                              Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }

    @Override
    public void eraseCredentials() {
        Object credentials = super.getCredentials();
        if (credentials instanceof String) {
            Utils.clearString((String)credentials);
        }
        super.eraseCredentials();
    }
}

package com.github.vssavin.umlib.security.spring;

import com.github.vssavin.umlib.config.UmConfig;
import com.github.vssavin.umlib.security.SecureService;
import com.github.vssavin.umlib.user.UserService;
import com.github.vssavin.umlib.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import javax.security.auth.login.AccountLockedException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vssavin on 18.12.2021
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userService;
    private final PasswordEncoder passwordEncoder;
    private final SecureService secureService;

    @Autowired
    public CustomAuthenticationProvider(UserService userService, PasswordEncoder passwordEncoder,
                                        UmConfig umConfig) {
        this.secureService = umConfig.getAuthService();
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Object credentials = authentication.getCredentials();
        Object userName = authentication.getPrincipal();
        if (credentials != null) {
            UserDetails user = userService.loadUserByUsername(userName.toString());
            if (user != null) {
                if (!user.isAccountNonExpired())
                    throw new AccountExpiredException("Account is expired!");
                if (!user.isAccountNonLocked())
                    throw new LockedException("Account is locked!");
                if(!user.isEnabled())
                    throw new DisabledException("Account is disabled!");
                Object details = authentication.getDetails();
                String addr = "";
                if (details instanceof WebAuthenticationDetails) {
                    addr = ((WebAuthenticationDetails) details).getRemoteAddress();
                }
                String password = secureService.decrypt(credentials.toString(), secureService.getSecureKey(addr));
                if (passwordEncoder.matches(password, user.getPassword())) {
                    List<GrantedAuthority> authorities = new ArrayList<>(user.getAuthorities());
                    return new CustomUsernamePasswordAuthenticationToken(authentication.getPrincipal(),
                            password, authorities);
                }
                else {
                    throw new BadCredentialsException("Authentication failed");
                }

            } else {
                return authentication;
            }

        } else {
            return authentication;
        }

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(CustomUsernamePasswordAuthenticationToken.class);
    }
}

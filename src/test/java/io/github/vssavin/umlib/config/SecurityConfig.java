package io.github.vssavin.umlib.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.sql.DataSource;

/**
 * @author vssavin on 18.12.2021
 */
@EnableWebSecurity
public class SecurityConfig extends DefaultSecurityConfig {

    static {
        DefaultSecurityConfig.setSuccessUrl("/games/index.html");
    }

    @Autowired
    public SecurityConfig(UmConfig umConfig, DataSource dataSource, AuthenticationSuccessHandler authSuccessHandler,
                          AuthenticationFailureHandler authFailureHandler, AuthenticationProvider authProvider,
                          PasswordEncoder passwordEncoder) {
        super(umConfig, dataSource, authSuccessHandler, authFailureHandler, authProvider, passwordEncoder);
    }
}

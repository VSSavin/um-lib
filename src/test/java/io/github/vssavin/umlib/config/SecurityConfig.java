package io.github.vssavin.umlib.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

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
    public SecurityConfig(UmConfig umConfig, @Qualifier("umDataSource") DataSource dataSource,
                          AuthenticationSuccessHandler authSuccessHandler,
                          AuthenticationFailureHandler authFailureHandler, AuthenticationProvider authProvider,
                          LogoutHandler logoutHandler, LogoutSuccessHandler logoutSuccessHandler,
                          PasswordEncoder passwordEncoder) {
        super(umConfig, dataSource, authSuccessHandler, authFailureHandler, authProvider,
                logoutHandler, logoutSuccessHandler, passwordEncoder);
    }
}

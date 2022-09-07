package io.github.vssavin.umlib.config;

import io.github.vssavin.umlib.service.impl.CustomOAuth2UserService;
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
                          LogoutHandler logoutHandler, CustomOAuth2UserService customOAuth2UserService,
                          LogoutSuccessHandler logoutSuccessHandler, PasswordEncoder passwordEncoder,
                          OAuth2Config oAuth2Config) {
        super(umConfig, dataSource, authSuccessHandler, authFailureHandler, authProvider,
                logoutHandler, customOAuth2UserService, logoutSuccessHandler, passwordEncoder, oAuth2Config);
    }
}

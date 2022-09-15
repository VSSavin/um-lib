package io.github.vssavin.umlib.config;

import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * @author vssavin on 18.12.2021
 */
@EnableWebSecurity
@Import(DefaultSecurityConfig.class)
public class SecurityConfig {

    static {
        DefaultSecurityConfig.setSuccessUrl("/games/index.html");
    }
}

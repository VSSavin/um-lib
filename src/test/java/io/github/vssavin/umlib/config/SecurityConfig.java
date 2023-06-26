package io.github.vssavin.umlib.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * @author vssavin on 18.12.2021
 */
@EnableWebSecurity
@Import(DefaultSecurityConfig.class)
public class SecurityConfig {

    @Bean
    public UmConfigurer umConfigurer() {
        return new UmConfigurer().successUrl("/games/index.html");
    }
}

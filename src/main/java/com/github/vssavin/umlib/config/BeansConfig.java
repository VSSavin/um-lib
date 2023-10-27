package com.github.vssavin.umlib.config;

import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains default beans for user management.
 *
 * @author vssavin on 18.12.2021
 */
@Component
public class BeansConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JavaMailSender emailSender() {
        return new JavaMailSenderImpl();
    }

    @Bean
    public UmConfigurer defaultUmConfigurer() {
        Map<String, String[]> defaultResourceHandlers = new HashMap<>();
        defaultResourceHandlers.put("/js/**", new String[] { "classpath:/static/js/" });
        defaultResourceHandlers.put("/css/**", new String[] { "classpath:/static/css/" });
        defaultResourceHandlers.put("/flags/**", new String[] { "classpath:/static/flags/" });
        defaultResourceHandlers.put("/img/**", new String[] { "classpath:/static/img/" });

        UmConfigurer defaultUmConfigurer = new UmConfigurer();
        defaultUmConfigurer.resourceHandlers(defaultResourceHandlers);

        return defaultUmConfigurer;
    }

}

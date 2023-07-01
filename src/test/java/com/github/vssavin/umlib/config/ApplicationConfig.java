package com.github.vssavin.umlib.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import java.util.Collections;

/**
 * @author vssavin on 18.12.2021
 */
@Configuration
@ComponentScan({"com.github.vssavin.umlib"})
@EnableWebSecurity
@Import(DefaultSecurityConfig.class)
public class ApplicationConfig {

    @Bean
    public UmConfigurer umConfigurer() {
        return new UmConfigurer().successUrl("/testApplication/index.html");
    }

    @Bean
    public FilterRegistrationBean<HiddenHttpMethodFilter> hiddenHttpMethodFilter(){
        FilterRegistrationBean<HiddenHttpMethodFilter> filterBean =
                new FilterRegistrationBean<>(new HiddenHttpMethodFilter());
        filterBean.setUrlPatterns(Collections.singletonList("/*"));
        return filterBean;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }
}

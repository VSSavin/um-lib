package io.github.vssavin.umlib.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import java.util.Collections;

/**
 * @author vssavin on 18.12.2021
 */
@Configuration
@EnableTransactionManagement
@ComponentScan({"io.github.vssavin.umlib"})
public class ApplicationConfig {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationConfig.class);

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

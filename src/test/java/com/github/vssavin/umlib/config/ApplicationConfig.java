package com.github.vssavin.umlib.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.Properties;

/**
 * @author vssavin on 18.12.2021
 */
@Configuration
@ComponentScan({"com.github.vssavin.umlib"})
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.github.vssavin.umlib.user")
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
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource routingDataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();

        try {
            em.setDataSource(routingDataSource);
            em.setPackagesToScan("com.github.vssavin.umlib.user");

            JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            em.setJpaVendorAdapter(vendorAdapter);
            String hibernateDialect = "org.hibernate.dialect.H2Dialect";

            Properties additionalProperties = new Properties();
            additionalProperties.put("hibernate.dialect", hibernateDialect);
            em.setJpaProperties(additionalProperties);
        } catch (Exception e) {
            System.out.println("Creating LocalContainerEntityManagerFactoryBean error!");
        }

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);

        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }
}

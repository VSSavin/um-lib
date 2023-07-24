package com.github.vssavin.umlib.config;

import com.github.vssavin.umlib.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
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
@EnableJpaRepositories(basePackageClasses = User.class)
@EnableWebSecurity
@Import(DefaultSecurityConfig.class)
public class ApplicationConfig {
    private static final Logger log = LoggerFactory.getLogger(ApplicationConfig.class);

    @Bean
    public UmConfigurer umConfigurer() {
        return new UmConfigurer().successUrl("/index.html")
                .permission(new AuthorizedUrlPermission("/index.html", Permission.ANY_USER))
                .permission(new AuthorizedUrlPermission("/index", Permission.ANY_USER))
                .configure();
    }

    @Bean
    public FilterRegistrationBean<HiddenHttpMethodFilter> hiddenHttpMethodFilter() {
        FilterRegistrationBean<HiddenHttpMethodFilter> filterBean =
                new FilterRegistrationBean<>(new HiddenHttpMethodFilter());
        filterBean.setUrlPatterns(Collections.singletonList("/*"));
        return filterBean;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource routingDataSource,
                                                                       DatabaseConfig databaseConfig) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();

        try {
            em.setDataSource(routingDataSource);
            em.setPackagesToScan(User.class.getPackage().getName());

            em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            String hibernateDialect = databaseConfig.getDialect();

            Properties additionalProperties = new Properties();
            additionalProperties.put("hibernate.dialect", hibernateDialect);
            em.setJpaProperties(additionalProperties);
        } catch (Exception e) {
            log.error("Creating LocalContainerEntityManagerFactoryBean error!", e);
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

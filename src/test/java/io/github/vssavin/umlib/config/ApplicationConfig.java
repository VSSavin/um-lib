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
//@EnableJpaRepositories(basePackages = "io.github.vssavin.umlib.repository")
public class ApplicationConfig {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationConfig.class);

    @Bean
    public FilterRegistrationBean<HiddenHttpMethodFilter> hiddenHttpMethodFilter(){
        FilterRegistrationBean<HiddenHttpMethodFilter> filterBean =
                new FilterRegistrationBean<>(new HiddenHttpMethodFilter());
        filterBean.setUrlPatterns(Collections.singletonList("/*"));
        return filterBean;
    }

    /*
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource routingDataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();

        try {
            em.setDataSource(routingDataSource);
            em.setPackagesToScan("io.github.vssavin.umlib.entity");

            JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            em.setJpaVendorAdapter(vendorAdapter);
            String hibernateDialect = "org.hibernate.dialect.H2Dialect";

            Properties additionalProperties = new Properties();
            additionalProperties.put("hibernate.dialect", hibernateDialect);
            em.setJpaProperties(additionalProperties);
        } catch (Exception e) {
            LOG.error("Creating LocalContainerEntityManagerFactoryBean error: ", e);
        }

        return em;
    }
    */

    /*
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);

        return transactionManager;
    }
    */

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }
}

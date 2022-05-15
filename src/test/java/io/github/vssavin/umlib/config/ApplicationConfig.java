package io.github.vssavin.umlib.config;

import io.github.vssavin.securelib.platformSecure.PlatformSpecificSecure;
import io.github.vssavin.umlib.utils.UmUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

/**
 * @author vssavin on 18.12.2021
 */
@Configuration
@EnableTransactionManagement
@ComponentScan({"io.github.vssavin.umlib"})
@EnableJpaRepositories(basePackages = "io.github.vssavin.umlib.repository")
public class ApplicationConfig {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationConfig.class);

    private DatabaseConfig databaseConfig;
    private EmailConfig emailConfig;
    private PlatformSpecificSecure secureService;

    //need for create ApplicationUtil (process application args) bean before create ApplicationConfig
    private UmUtil umUtil;

    @Autowired
    public ApplicationConfig(DatabaseConfig databaseConfig, EmailConfig emailConfig, UmUtil umUtil,
                             @Qualifier("applicationSecureService") PlatformSpecificSecure secureService) {
        this.databaseConfig = databaseConfig;
        this.emailConfig = emailConfig;
        this.secureService = secureService;
        this.umUtil = umUtil;
        System.setProperty("spring.datasource.data", "classpath:init.sql");
        System.setProperty("spring.datasource.initialization-mode", "always");
    }

    @Profile("prod")
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();

        try {
            em.setDataSource(dataSource());
            em.setPackagesToScan("io.github.vssavin.ppgames.entity");

            JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            em.setJpaVendorAdapter(vendorAdapter);
            String hibernateDialect = databaseConfig.getDialect();

            Properties additionalProperties = new Properties();

            if (hibernateDialect != null) {
                additionalProperties.put("hibernate.dialect", hibernateDialect);
            }

            em.setJpaProperties(additionalProperties);
        } catch (Exception e) {
            LOG.error("Creating LocalContainerEntityManagerFactoryBean error: ", e);
        }

        return em;
    }

    @Profile("dev")
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryForTest() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();

        try {
            em.setDataSource(dataSource());
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

    @Profile("prod")
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        try {
            dataSource.setDriverClassName(databaseConfig.getDriverClass());
            dataSource.setUrl(databaseConfig.getUrl() + "/" + databaseConfig.getName());
            dataSource.setUsername(databaseConfig.getUser());
            dataSource.setPassword(secureService.decrypt(databaseConfig.getPassword()));
        } catch (Exception e) {
            LOG.error("Creating datasource error: ", e);
        }


        return dataSource;
    }

    @Profile("dev")
    @Bean(name = "dataSource")
    public DataSource dataSourceForTest() {
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(H2)
                .setScriptEncoding("UTF-8")
                .ignoreFailedDrops(true)
                .addScript("init.sql")
                .build();
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

    @Profile("prod")
    @Bean
    public JavaMailSender emailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailConfig.getHost());
        mailSender.setPort(emailConfig.getPort());

        mailSender.setUsername(emailConfig.getUserName());
        mailSender.setPassword(secureService.decrypt(emailConfig.getPassword()));

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", emailConfig.getProtocol());
        props.put("mail.smtp.auth", emailConfig.isSmtpAuth());
        props.put("mail.smtp.starttls.enable", emailConfig.isTlsEnabled());
        props.put("mail.debug", "true");

        return mailSender;
    }

    @Profile("dev")
    @Bean(name = "emailSender")
    public JavaMailSender emailSenderForTest() {
        return new JavaMailSenderImpl();
    }
}

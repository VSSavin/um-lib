package com.github.vssavin.umlib.config;

import com.github.vssavin.umlib.domain.security.SecureService;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.Locale;

import static com.github.vssavin.umlib.config.UmSecureServiceArgumentsHandler.SECURE_SERVICE_PROP_NAME;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConfigureSecureServiceTest {

    private final UmConfigurer defaultConfigurer = new UmConfigurer();

    private final String defaultApplicationArgsBeanName = "springApplicationArguments";

    private final AbstractApplicationContext context = new AnnotationConfigServletWebApplicationContext();

    @Mock
    private SecureService aesSecureService;

    @Mock
    private SecureService rsaSecureService;

    @Mock
    private SecureService noSecureService;

    @Before
    public void setUp() {
        context.refresh();
        context.getBeanFactory().registerSingleton(defaultApplicationArgsBeanName, new DefaultApplicationArguments());

        when(aesSecureService.toString()).thenReturn("AES");
        when(rsaSecureService.toString()).thenReturn("RSA");
        when(noSecureService.toString()).thenReturn("NO");

        context.getBeanFactory().registerSingleton("aesSecureService", aesSecureService);
        context.getBeanFactory().registerSingleton("rsaSecureService", rsaSecureService);
        context.getBeanFactory().registerSingleton("noSecureService", noSecureService);
    }

    @Test
    public void authServiceFromApplicationArgsSuccess() {

        prepareAESServiceData();
        UmConfig umConfig = createUmConfig();
        Assertions.assertTrue(umConfig.getSecureService().toString().toUpperCase(Locale.ROOT).contains("AES"));

        prepareRSAServiceData();
        umConfig = createUmConfig();
        Assertions.assertTrue(umConfig.getSecureService().toString().toUpperCase(Locale.ROOT).contains("RSA"));

        prepareNoSecureServiceData();
        umConfig = createUmConfig();
        Assertions.assertTrue(umConfig.getSecureService().toString().toUpperCase(Locale.ROOT).contains("NO"));
    }

    @Test
    public void authServiceFromPropertiesSuccess() {
        prepareNoArgsData();

        System.setProperty(SECURE_SERVICE_PROP_NAME, "aes");
        UmConfig umConfig = createUmConfig();
        Assertions.assertTrue(umConfig.getSecureService().toString().toUpperCase(Locale.ROOT).contains("AES"));

        System.setProperty(SECURE_SERVICE_PROP_NAME, "rsa");
        umConfig = createUmConfig();
        Assertions.assertTrue(umConfig.getSecureService().toString().toUpperCase(Locale.ROOT).contains("RSA"));

        System.setProperty(SECURE_SERVICE_PROP_NAME, "no");
        umConfig = createUmConfig();
        Assertions.assertTrue(umConfig.getSecureService().toString().toUpperCase(Locale.ROOT).contains("NO"));

    }

    @Test
    public void authServiceMixedPropertiesAndApplicationArgumentsSuccess() {
        prepareAESServiceData();
        System.setProperty(SECURE_SERVICE_PROP_NAME, "rsa");
        UmConfig umConfig = createUmConfig();
        Assertions.assertTrue(umConfig.getSecureService().toString().toUpperCase(Locale.ROOT).contains("AES"));

        prepareRSAServiceData();
        System.setProperty(SECURE_SERVICE_PROP_NAME, "aes");
        umConfig = createUmConfig();
        Assertions.assertTrue(umConfig.getSecureService().toString().toUpperCase(Locale.ROOT).contains("RSA"));

        prepareNoSecureServiceData();
        System.setProperty(SECURE_SERVICE_PROP_NAME, "no");
        umConfig = createUmConfig();
        Assertions.assertTrue(umConfig.getSecureService().toString().toUpperCase(Locale.ROOT).contains("NO"));

    }

    private Object prepareNoArgsData() {
        String[] args = new String[] { "" };
        ApplicationArguments newApplicationArguments = new DefaultApplicationArguments(args);
        replaceSingletonBean(defaultApplicationArgsBeanName, newApplicationArguments);
        return newApplicationArguments;
    }

    private Object prepareAESServiceData() {
        String[] args = new String[] { "--" + SECURE_SERVICE_PROP_NAME + "=aes" };
        ApplicationArguments newApplicationArguments = new DefaultApplicationArguments(args);
        replaceSingletonBean(defaultApplicationArgsBeanName, newApplicationArguments);
        return newApplicationArguments;
    }

    private Object prepareRSAServiceData() {
        String[] args = new String[] { "--" + SECURE_SERVICE_PROP_NAME + "=rsa" };
        ApplicationArguments newApplicationArguments = new DefaultApplicationArguments(args);
        replaceSingletonBean(defaultApplicationArgsBeanName, newApplicationArguments);
        return newApplicationArguments;
    }

    private Object prepareNoSecureServiceData() {
        String[] args = new String[] { "--" + SECURE_SERVICE_PROP_NAME + "=no" };
        ApplicationArguments newApplicationArguments = new DefaultApplicationArguments(args);
        replaceSingletonBean(defaultApplicationArgsBeanName, newApplicationArguments);
        return newApplicationArguments;
    }

    private UmConfig createUmConfig() {
        UmSecureServiceArgumentsHandler argumentsHandler = new UmSecureServiceArgumentsHandler(context,
                SecureService.defaultSecureService());
        argumentsHandler.processArgs();
        return new UmConfig(defaultConfigurer, argumentsHandler);
    }

    private Object replaceSingletonBean(String beanName, Object replacement) {

        BeanDefinitionRegistry beanRegistry = (BeanDefinitionRegistry) context.getAutowireCapableBeanFactory();
        Object oldBean = null;

        try {
            oldBean = context.getBean(beanName);
            ((DefaultListableBeanFactory) beanRegistry).destroySingleton(beanName);
        }
        catch (NoSuchBeanDefinitionException ignore) {
            // ignore
            System.out.println();
        }

        try {
            context.getBean(beanName);
            throw new IllegalStateException("Can't remove the bean with name: " + beanName);
        }
        catch (NoSuchBeanDefinitionException ignore) {
            // ignore
        }

        ((DefaultListableBeanFactory) beanRegistry).registerSingleton(beanName, replacement);
        return oldBean;
    }

}

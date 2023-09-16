package com.github.vssavin.umlib.domain.security;

import com.github.vssavin.jcrypt.osplatform.OSPlatformCrypt;
import com.github.vssavin.umlib.config.UmConfig;
import com.github.vssavin.umlib.AbstractTest;
import com.github.vssavin.umlib.config.UmConfigurer;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ApplicationContext;

public class ConfigureSecureServiceTest extends AbstractTest {

    private final UmConfigurer defaultConfigurer = new UmConfigurer();

    private final String defaultApplicationArgsBeanName = "springApplicationArguments";

    private ApplicationContext context;

    @Autowired
    public void setContext(ApplicationContext context) {
        this.context = context;
    }

    @Test
    public void authServiceFromApplicationArgsSuccess() {

        Object defaultApplicationArgs = prepareAESServiceData();
        UmConfig umConfig = createUmConfig();
        Assertions.assertInstanceOf(AESSecureService.class, umConfig.getSecureService());

        prepareRSAServiceData();
        umConfig = createUmConfig();
        Assertions.assertInstanceOf(RSASecureService.class, umConfig.getSecureService());

        prepareNoSecureServiceData();
        umConfig = createUmConfig();
        Assertions.assertInstanceOf(NoSecureService.class, umConfig.getSecureService());

        if (defaultApplicationArgs != null) {
            replaceSingletonBean(defaultApplicationArgsBeanName, defaultApplicationArgs);
        }
        umConfig = createUmConfig();
        Assertions.assertInstanceOf(secureService.getClass(), umConfig.getSecureService());
    }

    @Test
    public void authServiceFromPropertiesSuccess() {
        Object defaultApplicationArgs = prepareNoArgsData();

        System.setProperty("authService", "aes");
        UmConfig umConfig = createUmConfig();
        Assertions.assertInstanceOf(AESSecureService.class, umConfig.getSecureService());
        System.setProperty("authService", "rsa");
        umConfig = createUmConfig();
        Assertions.assertInstanceOf(RSASecureService.class, umConfig.getSecureService());
        System.setProperty("authService", "no");
        umConfig = createUmConfig();
        Assertions.assertInstanceOf(NoSecureService.class, umConfig.getSecureService());
        System.clearProperty("authService");

        if (defaultApplicationArgs != null) {
            replaceSingletonBean(defaultApplicationArgsBeanName, defaultApplicationArgs);
        }
        umConfig = createUmConfig();
        Assertions.assertInstanceOf(secureService.getClass(), umConfig.getSecureService());
    }

    @Test
    public void authServiceMixedPropertiesAndApplicationArgumentsSuccess() {
        Object defaultApplicationArgs = prepareAESServiceData();
        System.setProperty("authService", "rsa");
        UmConfig umConfig = createUmConfig();
        Assertions.assertInstanceOf(AESSecureService.class, umConfig.getSecureService());

        prepareRSAServiceData();
        System.setProperty("authService", "aes");
        umConfig = createUmConfig();
        Assertions.assertInstanceOf(RSASecureService.class, umConfig.getSecureService());

        prepareNoSecureServiceData();
        System.setProperty("authService", "rsa");
        umConfig = createUmConfig();
        Assertions.assertInstanceOf(NoSecureService.class, umConfig.getSecureService());

        if (defaultApplicationArgs != null) {
            replaceSingletonBean(defaultApplicationArgsBeanName, defaultApplicationArgs);
        }
        umConfig = createUmConfig();
        Assertions.assertInstanceOf(secureService.getClass(), umConfig.getSecureService());
    }

    private Object prepareNoArgsData() {
        String[] args = new String[] { "" };
        ApplicationArguments newApplicationArguments = new DefaultApplicationArguments(args);
        return replaceSingletonBean(defaultApplicationArgsBeanName, newApplicationArguments);
    }

    private Object prepareAESServiceData() {
        String[] args = new String[] { "--authService=aes" };
        ApplicationArguments newApplicationArguments = new DefaultApplicationArguments(args);
        return replaceSingletonBean(defaultApplicationArgsBeanName, newApplicationArguments);
    }

    private Object prepareRSAServiceData() {
        String[] args = new String[] { "--authService=rsa" };
        ApplicationArguments newApplicationArguments = new DefaultApplicationArguments(args);
        return replaceSingletonBean(defaultApplicationArgsBeanName, newApplicationArguments);
    }

    private Object prepareNoSecureServiceData() {
        String[] args = new String[] { "--authService=no" };
        ApplicationArguments newApplicationArguments = new DefaultApplicationArguments(args);
        return replaceSingletonBean(defaultApplicationArgsBeanName, newApplicationArguments);
    }

    private UmConfig createUmConfig() {
        OSPlatformCrypt applicationSecureService = (OSPlatformCrypt) context.getBean("applicationSecureService");
        return new UmConfig(context, secureService, defaultConfigurer, applicationSecureService);
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

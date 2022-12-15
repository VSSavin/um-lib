package io.github.vssavin.umlib.tests.unit;

import io.github.vssavin.securelib.platformSecure.PlatformSpecificSecure;
import io.github.vssavin.umlib.service.SecureService;
import io.github.vssavin.umlib.service.impl.AESSecureService;
import io.github.vssavin.umlib.service.impl.NoSecureService;
import io.github.vssavin.umlib.service.impl.RSASecureService;
import io.github.vssavin.umlib.tests.AbstractTest;
import io.github.vssavin.umlib.utils.UmUtil;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ApplicationContext;

public class UmUtilTest extends AbstractTest {

    private final String defaultApplicationArgsBeanName = "springApplicationArguments";
    private SecureService secureService;
    private ApplicationContext context;

    @Autowired
    public void setContext(ApplicationContext context) {
        this.context = context;
    }

    @Autowired
    public void setApplicationUtil(UmUtil umUtil) {
        secureService = umUtil.getAuthService();
    }

    @Test
    public void authServiceFromApplicationArgsSuccess() {

        Object defaultApplicationArgs = prepareAESServiceData();
        UmUtil umUtil = createUmUtil();
        Assertions.assertInstanceOf(AESSecureService.class, umUtil.getAuthService());

        prepareRSAServiceData();
        umUtil = createUmUtil();
        Assertions.assertInstanceOf(RSASecureService.class, umUtil.getAuthService());

        prepareNoSecureServiceData();
        umUtil = createUmUtil();
        Assertions.assertInstanceOf(NoSecureService.class, umUtil.getAuthService());

        if (defaultApplicationArgs != null) replaceSingletonBean(defaultApplicationArgsBeanName, defaultApplicationArgs);
        umUtil = createUmUtil();
        Assertions.assertInstanceOf(secureService.getClass(), umUtil.getAuthService());
    }

    @Test
    public void authServiceFromPropertiesSuccess() {
        Object defaultApplicationArgs = prepareNoArgsData();

        System.setProperty("authService", "aes");
        UmUtil umUtil = createUmUtil();
        Assertions.assertInstanceOf(AESSecureService.class, umUtil.getAuthService());
        System.setProperty("authService", "rsa");
        umUtil = createUmUtil();
        Assertions.assertInstanceOf(RSASecureService.class, umUtil.getAuthService());
        System.setProperty("authService", "no");
        umUtil = createUmUtil();
        Assertions.assertInstanceOf(NoSecureService.class, umUtil.getAuthService());
        System.clearProperty("authService");

        if (defaultApplicationArgs != null) replaceSingletonBean(defaultApplicationArgsBeanName, defaultApplicationArgs);
        umUtil = createUmUtil();
        Assertions.assertInstanceOf(secureService.getClass(), umUtil.getAuthService());
    }

    @Test
    public void authServiceMixedPropertiesAndApplicationArgumentsSuccess() {
        Object defaultApplicationArgs = prepareAESServiceData();
        System.setProperty("authService", "rsa");
        UmUtil umUtil = createUmUtil();
        Assertions.assertInstanceOf(AESSecureService.class, umUtil.getAuthService());

        prepareRSAServiceData();
        System.setProperty("authService", "aes");
        umUtil = createUmUtil();
        Assertions.assertInstanceOf(RSASecureService.class, umUtil.getAuthService());

        prepareNoSecureServiceData();
        System.setProperty("authService", "rsa");
        umUtil = createUmUtil();
        Assertions.assertInstanceOf(NoSecureService.class, umUtil.getAuthService());

        if (defaultApplicationArgs != null) replaceSingletonBean(defaultApplicationArgsBeanName, defaultApplicationArgs);
        umUtil = createUmUtil();
        Assertions.assertInstanceOf(secureService.getClass(), umUtil.getAuthService());
    }

    private Object prepareNoArgsData() {
        String[] args = new String[]{""};
        ApplicationArguments newApplicationArguments = new DefaultApplicationArguments(args);
        return replaceSingletonBean(defaultApplicationArgsBeanName, newApplicationArguments);
    }

    private Object prepareAESServiceData() {
        String[] args = new String[]{"--authService=aes"};
        ApplicationArguments newApplicationArguments = new DefaultApplicationArguments(args);
        return replaceSingletonBean(defaultApplicationArgsBeanName, newApplicationArguments);
    }

    private Object prepareRSAServiceData() {
        String[] args = new String[]{"--authService=rsa"};
        ApplicationArguments newApplicationArguments = new DefaultApplicationArguments(args);
        return replaceSingletonBean(defaultApplicationArgsBeanName, newApplicationArguments);
    }

    private Object prepareNoSecureServiceData() {
        String[] args = new String[]{"--authService=no"};
        ApplicationArguments newApplicationArguments = new DefaultApplicationArguments(args);
        return replaceSingletonBean(defaultApplicationArgsBeanName, newApplicationArguments);
    }

    private UmUtil createUmUtil() {
        PlatformSpecificSecure applicationSecureService =
                (PlatformSpecificSecure) context.getBean("applicationSecureService");
        UmUtil.setApplicationArguments(null);
        return new UmUtil(context, secureService, applicationSecureService);
    }

    private Object replaceSingletonBean(String beanName, Object replacement) {

        BeanDefinitionRegistry beanRegistry = (BeanDefinitionRegistry) context.getAutowireCapableBeanFactory();
        Object oldBean = null;

        try {
            oldBean = context.getBean(beanName);
            ((DefaultListableBeanFactory) beanRegistry).destroySingleton(beanName);
        } catch (NoSuchBeanDefinitionException ignore) {
            System.out.println();
        }

        try {
            context.getBean(beanName);
            throw new IllegalStateException("Can't remove the bean with name: " + beanName);
        } catch (NoSuchBeanDefinitionException ignore) {}

        ((DefaultListableBeanFactory) beanRegistry).registerSingleton(beanName, replacement);
        return oldBean;
    }
}

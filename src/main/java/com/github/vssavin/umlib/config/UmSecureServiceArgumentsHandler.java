package com.github.vssavin.umlib.config;

import com.github.vssavin.umlib.domain.security.SecureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Map;

/**
 * An {@link com.github.vssavin.umlib.config.AbstractApplicationArgumentsHandler}
 * implementation that processes application arguments to initialize a secure service.
 *
 * @author vssavin on 16.09.2023
 */
@Configuration
class UmSecureServiceArgumentsHandler extends AbstractApplicationArgumentsHandler {

    static final String SECURE_SERVICE_PROP_NAME = "um.secureService";

    private static final Logger log = LoggerFactory.getLogger(UmSecureServiceArgumentsHandler.class);

    private final ApplicationContext context;

    private final SecureService defaultSecureService;

    private SecureService secureService;

    @Value("${" + SECURE_SERVICE_PROP_NAME + ":#{null}}")
    private String secureServiceName;

    @Autowired
    UmSecureServiceArgumentsHandler(ApplicationContext applicationContext, SecureService secureService) {
        super(log, applicationContext);
        this.context = applicationContext;
        this.defaultSecureService = secureService;
    }

    @PostConstruct
    @Override
    protected void processArgs() {
        String[] args = getApplicationArguments();
        if (args.length > 0) {
            String argsString = Arrays.toString(args);
            log.debug("Application started with arguments: {}", argsString);
            Map<String, String> mappedArgs = getMappedArgs(args);

            String serviceName = mappedArgs.get(SECURE_SERVICE_PROP_NAME);

            if (serviceName != null) {
                this.secureServiceName = serviceName;
            }
        }

        if (this.secureServiceName == null) {
            String serviceName = System.getProperty(SECURE_SERVICE_PROP_NAME);
            if (serviceName != null) {
                this.secureServiceName = serviceName;
            }
        }

        if (this.secureServiceName == null) {
            String serviceName = System.getenv(SECURE_SERVICE_PROP_NAME);
            if (serviceName != null) {
                this.secureServiceName = serviceName;
            }
        }

        initSecureService(secureServiceName);
    }

    SecureService getSecureService() {
        return secureService;
    }

    private void initSecureService(String secureServiceName) {
        if (context == null || defaultSecureService == null) {
            throw new IllegalStateException("Not initialized application context or default secure service!");
        }

        if (secureServiceName != null && !secureServiceName.isEmpty()) {
            secureService = getSecureServiceByName(secureServiceName);
        }
        else {
            log.warn("Secure service not specified! Using default secure service...");
            secureService = defaultSecureService;
        }
    }

    private SecureService getSecureServiceByName(String serviceName) {
        SecureService service = null;
        boolean beanFound = true;
        try {
            service = (SecureService) context.getBean(serviceName + "SecureService");
        }
        catch (NoSuchBeanDefinitionException ignore) {
            try {
                service = (SecureService) context.getBean(serviceName.toUpperCase() + "SecureService");
            }
            catch (NoSuchBeanDefinitionException e) {
                beanFound = false;
            }
        }
        if (!beanFound) {
            throw new IllegalArgumentException(String.format("Service with name %s not found!", serviceName));
        }
        return service;
    }

}

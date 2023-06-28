package io.github.vssavin.umlib.config;

import io.github.vssavin.securelib.Utils;
import io.github.vssavin.securelib.platformSecure.PlatformSpecificSecure;
import io.github.vssavin.umlib.security.SecureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author vssavin on 15.05.2022
 */
@Configuration
@PropertySource(value = "file:./conf.properties", encoding = "UTF-8")
public class UmConfig extends StorableConfig{
    @IgnoreField public static final String LOGIN_URL = "/login";
    @IgnoreField public static final String LOGIN_PROCESSING_URL = "/perform-login";
    @IgnoreField public static final String LOGOUT_URL = "/logout";

    @IgnoreField public static String adminSuccessUrl = "/um/admin";
    @IgnoreField public static String successUrl = "/index.html";

    @IgnoreField private static final String NAME_PREFIX = "application";
    @IgnoreField private static final String CONFIG_FILE = "conf.properties";

    private static final Logger log = LoggerFactory.getLogger(UmConfig.class);
    private String[] applicationArgs;
    private final ApplicationContext context;
    private final SecureService defaultSecureService;
    private final PlatformSpecificSecure encryptPropertiesPasswordService;
    private final UmConfigurer umConfigurer;
    private SecureService authService;

    @Value("${" + NAME_PREFIX + ".url}")
    private String applicationUrl;

    @Value("${um.registration.allowed:true}")
    private Boolean registrationAllowed;

    @Value("${um.login.title:}")
    private String loginTitle;

    private static final List<AuthorizedUrlPermission> authorizedUrlPermissions = new ArrayList<>();

    private boolean permissionsUpdated = false;

    static
    {
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/js/**", new String[0]));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/css/**", new String[0]));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/um/users/passwordRecovery", new String[0]));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/um/users/perform-password-recovery", new String[0]));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/um/users/registration", new String[0]));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/um/users/perform-register", new String[0]));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/um/users/confirmUser", new String[0]));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/um/admin**", new String[]{"ADMIN"}));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/um/admin/**", new String[]{"ADMIN"}));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/um/users/**", new String[]{"ADMIN", "USER"}));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/perform-logout", new String[0]));
    }

    public UmConfig(ApplicationContext context, SecureService secureService, UmConfigurer umConfigurer,
                    @Qualifier("applicationSecureService") PlatformSpecificSecure applicationSecureService) {
        super.setConfigFile(CONFIG_FILE);
        super.setNamePrefix(NAME_PREFIX);
        this.context = context;
        this.defaultSecureService = secureService;
        this.authService = defaultSecureService;
        this.umConfigurer = umConfigurer;
        this.encryptPropertiesPasswordService = applicationSecureService;
        if (applicationArgs == null || applicationArgs.length == 0) {
            String[] args = getAppArgsFromContext(context);
            if (args.length > 0) applicationArgs = args;
        }
        initSecureService("");
        processArgs(applicationArgs);
        log.debug("Using auth service: " + authService);
    }

    public SecureService getAuthService() {
        return authService;
    }

    public static List<AuthorizedUrlPermission> getAuthorizedUrlPermissions() {return authorizedUrlPermissions;}

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

    public Boolean getRegistrationAllowed() {
        return registrationAllowed;
    }

    public String getLoginTitle() {
        return loginTitle;
    }

    public void updateAuthorizedPermissions() {
        if (!permissionsUpdated) {
            if (!registrationAllowed) {
                int registrationIndex = -1, performRegisterIndex = -1;

                for(int i = 0; i < authorizedUrlPermissions.size(); i++) {
                    AuthorizedUrlPermission authorizedUrlPermission = authorizedUrlPermissions.get(i);
                    if (authorizedUrlPermission.getUrl().equals("/um/users/registration")) {
                        registrationIndex = i;
                    } else if (authorizedUrlPermission.getUrl().equals("/um/users/perform-register")) {
                        performRegisterIndex = i;
                    }
                }

                if (registrationIndex != -1) {
                    authorizedUrlPermissions.set(registrationIndex,
                            new AuthorizedUrlPermission("/um/users/registration", new String[]{"ADMIN"}));
                }

                if (performRegisterIndex != -1) {
                    authorizedUrlPermissions.set(performRegisterIndex,
                            new AuthorizedUrlPermission("/um/users/perform-register", new String[]{"ADMIN"}));
                }

                permissionsUpdated = true;
            }
        }
    }

    private String[] getAppArgsFromContext(ApplicationContext context) {
        try {
            Object appArgsBean = context.getBean("springApplicationArguments");
            Method sourceArgesMethod = appArgsBean.getClass().getMethod("getSourceArgs");
            String[] args = (String[]) sourceArgesMethod.invoke(appArgsBean);
            if (args != null && args.length > 0) return args;
        } catch (NoSuchBeanDefinitionException ignore) {
        } catch (NoSuchMethodException e) {
            log.error("Method \"getSourceArgs\" not found!", e);
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.error("Method invocation error", e);
        }
        return new String[]{};
    }

    private void initSecureService(String secureServiceName) {
        if (context == null || defaultSecureService == null) {
            throw new IllegalStateException("Not initialized application context or default secure service!");
        }

        if (secureServiceName != null && !secureServiceName.isEmpty()) {
            authService = getSecureServiceByName(secureServiceName);
        }

        else {
            String authServiceName = System.getProperty("authService");
            if (authServiceName != null && !authServiceName.isEmpty()) {
                authService = getSecureServiceByName(authServiceName);
            }
            else {
                authService = umConfigurer.getSecureService();
            }
        }
    }

    private SecureService getSecureServiceByName(String serviceName) {
        SecureService secureService = null;
        boolean beanFound = true;
        try {
            secureService = (SecureService) context.getBean(serviceName + "SecureService");
        } catch (NoSuchBeanDefinitionException ignore) {
            try {
                secureService = (SecureService) context.getBean(serviceName.toUpperCase() + "SecureService");
            } catch (NoSuchBeanDefinitionException e) {
                beanFound = false;
            }
        }
        if (!beanFound)
            throw new NoSuchSecureServiceException(String.format("Service with name %s not found!", serviceName));
        return secureService;
    }

    private void processArgs(String[] args) {
        if (args != null && args.length > 0) {
            System.out.println("Application started with arguments: " + Arrays.toString(args));
            Map<String, String> mappedArgs = getMappedArgs(args);
            String password = mappedArgs.get("ep");
            if (password != null) {
                String encrypted = encryptPropertiesPasswordService.encrypt(password, "");
                Utils.clearString(password);
                System.out.printf("Encryption for password [%s] : %s%n", password, encrypted);
                Utils.clearString(password);
            }
            String authServiceName = mappedArgs.get("authService");
            if (authServiceName != null) initSecureService(authServiceName);
        }
        else {
            log.warn("Unknown application arguments!");
        }
    }

    private static Map<String, String> getMappedArgs(String[] args) {
        Map<String, String> resultMap = new HashMap<>();
        if (args.length > 0) {
            for(String arg : args) {
                String[] params = arg.replaceAll("--", "").split("=");
                if (params.length > 0) {
                    String value = params.length > 1 ? params[1] : "";
                    resultMap.put(params[0], value);
                }
            }
        }
        return resultMap;
    }

    private static class NoSuchSecureServiceException extends RuntimeException {

        NoSuchSecureServiceException(String message) {
            super(message);
        }
    }
}

package io.github.vssavin.umlib.utils;

import io.github.vssavin.securelib.platformSecure.PlatformSpecificSecure;
import io.github.vssavin.umlib.service.SecureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author vssavin on 25.01.22
 */
@Component
public class UmUtil {
    private static final Logger log = LoggerFactory.getLogger(UmUtil.class);
    private static String[] applicationArgs;
    private final ApplicationContext context;
    private final SecureService defaultSecureService;
    private final PlatformSpecificSecure encryptPropertiesPasswordService;
    private SecureService authService;

    public UmUtil(ApplicationContext context, SecureService secureService,
                  @Qualifier("applicationSecureService") PlatformSpecificSecure applicationSecureService) {
        this.context = context;
        this.defaultSecureService = secureService;
        this.authService = defaultSecureService;
        this.encryptPropertiesPasswordService = applicationSecureService;
        processArgs(applicationArgs);
    }

    public static void setApplicationArguments(String[] args) {
        applicationArgs = args;
    }

    public SecureService getAuthService() {
        return authService;
    }

    private void processArgs(String[] args) {
        if (args != null) {
            System.out.println("Application started with arguments: " + Arrays.toString(args));
            Map<String, String> mappedArgs = getMappedArgs(args);
            String value = mappedArgs.get("ep");
            if (value != null) {
                System.out.printf("Encrypt for value [%s] : %s%n", value,
                        encryptPropertiesPasswordService.encrypt(value, ""));
            }
            String authServiceName = mappedArgs.get("authService");
            if (authServiceName == null) authServiceName = System.getProperty("authService");
            if (authServiceName != null) {
                if (context == null || defaultSecureService == null) {
                    throw new RuntimeException("Not initialized application context or default secure service!");
                }
                if (!authServiceName.isEmpty()) {
                    if (authServiceName.equalsIgnoreCase("aes")) {
                        authService = (SecureService) context.getBean("AESSecureService");
                    }
                    else if (authServiceName.equalsIgnoreCase("rsa")) {
                        authService = (SecureService) context.getBean("RSASecureService");
                    }
                }
            }
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
}

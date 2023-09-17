package com.github.vssavin.umlib.config;

import com.github.vssavin.jcrypt.DefaultStringSafety;
import com.github.vssavin.jcrypt.StringSafety;
import com.github.vssavin.jcrypt.osplatform.OSPlatformCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Map;

/**
 * An {@link com.github.vssavin.umlib.config.AbstractApplicationArgumentsHandler}
 * implementation that processes application arguments to encode password.
 *
 * @author vssavin on 16.09.2023
 */
@Configuration
class UmPasswordEncodingArgumentsHandler extends AbstractApplicationArgumentsHandler {

    private static final Logger log = LoggerFactory.getLogger(UmPasswordEncodingArgumentsHandler.class);

    @Value("${um.ep:#{null}}")
    private String encodePassword;

    @Value("${um.encode-db-password:#{null}}")
    private String encodeDbPassword;

    private final StringSafety stringSafety = new DefaultStringSafety();

    private final OSPlatformCrypt encryptPropertiesPasswordService;

    @Autowired
    UmPasswordEncodingArgumentsHandler(ApplicationContext applicationContext,
            @Qualifier("applicationSecureService") OSPlatformCrypt applicationSecureService) {
        super(log, applicationContext);
        this.encryptPropertiesPasswordService = applicationSecureService;
    }

    @PostConstruct
    @Override
    protected void processArgs() {
        String[] args = getApplicationArguments();
        if (args.length > 0) {
            String argsString = Arrays.toString(args);
            log.debug("Application started with arguments: {}", argsString);
            Map<String, String> mappedArgs = getMappedArgs(args);
            String password = mappedArgs.get("ep");
            if (password != null) {
                String encrypted = encryptPropertiesPasswordService.encrypt(password, "");
                stringSafety.clearString(password);
                log.debug("Encryption for password [{}] : {}", password, encrypted);
                stringSafety.clearString(password);
            }
        }
    }

}

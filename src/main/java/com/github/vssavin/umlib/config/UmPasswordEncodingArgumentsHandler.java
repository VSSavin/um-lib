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
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * An {@link com.github.vssavin.umlib.config.AbstractApplicationArgumentsHandler}
 * implementation that processes application arguments to encode password.
 *
 * @author vssavin on 16.09.2023
 */
@Configuration
class UmPasswordEncodingArgumentsHandler extends AbstractApplicationArgumentsHandler {

    static final String PRINT_ENCODE_PASSWORD_PROP_NAME = "um.ep";

    static final String DB_PASSWORD_ENCODED_PROP_NAME = "um.db-password-encoded";

    private static final Logger log = LoggerFactory.getLogger(UmPasswordEncodingArgumentsHandler.class);

    @Value("${" + PRINT_ENCODE_PASSWORD_PROP_NAME + ":#{null}}")
    private String password;

    @Value("${" + DB_PASSWORD_ENCODED_PROP_NAME + ":#{false}}")
    private boolean dbPasswordEncoded;

    private final StringSafety stringSafety = new DefaultStringSafety();

    private final OSPlatformCrypt passwordService;

    private final PrintStream passwordPrintStream;

    @Autowired
    UmPasswordEncodingArgumentsHandler(ApplicationContext applicationContext,
            @Qualifier("applicationSecureService") OSPlatformCrypt applicationSecureService,
            @Autowired(required = false) PrintStream passwordPrintStream) {
        super(log, applicationContext);
        this.passwordPrintStream = passwordPrintStream;
        this.passwordService = applicationSecureService;
    }

    @PostConstruct
    @Override
    protected void processArgs() {
        String[] args = getApplicationArguments();
        if (args.length > 0) {
            String argsString = Arrays.toString(args);
            log.debug("Application started with arguments: {}", argsString);
            Map<String, String> mappedArgs = getMappedArgs(args);
            String pass = mappedArgs.get(PRINT_ENCODE_PASSWORD_PROP_NAME);
            if (pass != null) {
                printPassword(pass);
            }

            String passwordEncodedString = mappedArgs.get(DB_PASSWORD_ENCODED_PROP_NAME);
            if (passwordEncodedString != null) {
                this.dbPasswordEncoded = Boolean.parseBoolean(passwordEncodedString);
            }
        }
        else {
            if (password != null) {
                printPassword(password);
            }
        }
    }

    public boolean isDbPasswordEncoded() {
        return dbPasswordEncoded;
    }

    public OSPlatformCrypt getPasswordService() {
        return passwordService;
    }

    private void printPassword(String pass) {
        String messageText = "Encryption for password";
        String logMessageFormat = messageText + " [{}] : {}";
        String stringMessageFormat = "%s " + messageText + " [%s] : %s";
        if (pass != null) {
            String encrypted = passwordService.encrypt(pass, "");
            stringSafety.clearString(pass);
            if (passwordPrintStream == null) {
                log.debug(logMessageFormat, pass, encrypted);
            }
            else {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                passwordPrintStream
                    .println(String.format(stringMessageFormat, dateFormat.format(new Date()), pass, encrypted));
            }
            stringSafety.clearString(pass);
        }
    }

}

package io.github.vssavin.umlib.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author vssavin on 15.05.2022
 */
@Component
@PropertySource("file:./conf.properties")
public class UmConfig extends StorableConfig{
    @IgnoreField public static final String LOGIN_URL = "/login";
    @IgnoreField public static final String LOGIN_PROCESSING_URL = "/perform-login";
    @IgnoreField public static final String LOGOUT_URL = "/logout";

    @IgnoreField public static final String ADMIN_SUCCESS_URL = "/admin";
    @IgnoreField public static String successUrl = "/index.html";

    @IgnoreField private static final String NAME_PREFIX = "application";
    @IgnoreField private static final String CONFIG_FILE = "conf.properties";

    @Value("${" + NAME_PREFIX + ".url}")
    private String applicationUrl;

    public UmConfig() {
        super.setConfigFile(CONFIG_FILE);
        super.setNamePrefix(NAME_PREFIX);
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

    public static void setSuccessUrl(String successUrl) {
        UmConfig.successUrl = successUrl;
    }
}

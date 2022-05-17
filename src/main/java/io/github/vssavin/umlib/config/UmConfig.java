package io.github.vssavin.umlib.config;

import io.github.vssavin.umlib.utils.AuthorizedUrlPermission;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vssavin on 15.05.2022
 */
@Component
@PropertySource("file:./conf.properties")
public class UmConfig extends StorableConfig{
    @IgnoreField public static final String LOGIN_URL = "/login";
    @IgnoreField public static final String LOGIN_PROCESSING_URL = "/perform-login";
    @IgnoreField public static final String LOGOUT_URL = "/logout";

    @IgnoreField public static String adminSuccessUrl = "/admin";
    @IgnoreField public static String successUrl = "/index.html";

    @IgnoreField private static final String NAME_PREFIX = "application";
    @IgnoreField private static final String CONFIG_FILE = "conf.properties";

    @Value("${" + NAME_PREFIX + ".url}")
    private String applicationUrl;

    private static final List<AuthorizedUrlPermission> authorizedUrlPermissions = new ArrayList<>();

    static
    {
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/js/**", new String[0]));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/css/**", new String[0]));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/user/registration", new String[0]));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/user/perform-register", new String[0]));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/user/confirmUser", new String[0]));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/admin**", new String[]{"ADMIN"}));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/admin/**", new String[]{"ADMIN"}));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/user/**", new String[]{"ADMIN", "USER"}));
    }

    public UmConfig() {
        super.setConfigFile(CONFIG_FILE);
        super.setNamePrefix(NAME_PREFIX);
    }

    public static List<AuthorizedUrlPermission> getAuthorizedUrlPermissions() {return authorizedUrlPermissions;}

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

}

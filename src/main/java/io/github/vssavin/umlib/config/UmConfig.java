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

    @IgnoreField public static String adminSuccessUrl = "/um/admin";
    @IgnoreField public static String successUrl = "/index.html";

    @IgnoreField private static final String NAME_PREFIX = "application";
    @IgnoreField private static final String CONFIG_FILE = "conf.properties";

    @Value("${" + NAME_PREFIX + ".url}")
    private String applicationUrl;

    @Value("${um.registration.allowed:true}")
    private Boolean registrationAllowed;

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

    public Boolean getRegistrationAllowed() {
        return registrationAllowed;
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
}

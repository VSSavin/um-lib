package com.github.vssavin.umlib.config;

import com.github.vssavin.umlib.domain.security.SecureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;
import org.springframework.http.HttpMethod;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Base project configuration class.
 *
 * @author vssavin on 15.05.2022
 */
@Configuration
@PropertySource(value = "file:./conf.properties", encoding = "UTF-8")
public class UmConfig extends StorableConfig {

    @IgnoreField
    public static final String LOGIN_URL = "/login";

    @IgnoreField
    public static final String LOGIN_PROCESSING_URL = "/perform-login";

    @IgnoreField
    public static final String LOGOUT_URL = "/logout";

    @IgnoreField
    public static final String PERFORM_LOGOUT_URL = "/perform-logout";

    @IgnoreField
    public static final String REGISTRATION_URL = "/um/users/registration";

    @IgnoreField
    public static final String PERFORM_REGISTER_URL = "/um/users/perform-register";

    @IgnoreField
    public static final String ADMIN_PATH = "/um/admin/*";

    @IgnoreField
    private final String adminSuccessUrl;

    @IgnoreField
    private final String successUrl;

    @IgnoreField
    private static final String NAME_PREFIX = "application";

    @IgnoreField
    private static final String CONFIG_FILE = "conf.properties";

    private static final Logger log = LoggerFactory.getLogger(UmConfig.class);

    private final UmConfigurer umConfigurer;

    @Value("${" + NAME_PREFIX + ".url}")
    private String applicationUrl;

    @Value("${um.registration.allowed:true}")
    private boolean registrationAllowed;

    @Value("${um.login.title:}")
    private String loginTitle;

    @Value("${um.auth.maxFailureCount:3}")
    private int maxAuthFailureCount;

    @Value("${um.auth.failureBlockTimeMinutes:60}")
    private int authFailureBlockTime;

    private SecureService secureService;

    private final List<AuthorizedUrlPermission> authorizedUrlPermissions = new ArrayList<>();

    private final boolean csrfEnabled;

    @Autowired
    public UmConfig(UmConfigurer umConfigurer, UmSecureServiceArgumentsHandler umSecureServiceArgumentsHandler) {
        super.setConfigFile(CONFIG_FILE);
        super.setNamePrefix(NAME_PREFIX);
        secureService = umSecureServiceArgumentsHandler.getSecureService();
        this.umConfigurer = umConfigurer;
        this.csrfEnabled = umConfigurer.isCsrfEnabled();

        initDefaultPermissions();
        this.authorizedUrlPermissions.addAll(umConfigurer.getPermissions());
        adminSuccessUrl = umConfigurer.getAdminSuccessUrl();
        successUrl = umConfigurer.getSuccessUrl();
        log.debug("Using auth service: {}", this.secureService);
    }

    @PostConstruct
    private void updateAuthorizedPermissions() {
        if (!registrationAllowed) {
            updatePermission(REGISTRATION_URL, Permission.ADMIN_ONLY);
            updatePermission(PERFORM_REGISTER_URL, Permission.ADMIN_ONLY);
            updatePermission(PERFORM_REGISTER_URL, HttpMethod.POST.name(), Permission.ADMIN_ONLY);
        }
    }

    void setSecureService(SecureService secureService) {
        this.secureService = secureService;
    }

    public SecureService getSecureService() {
        return secureService;
    }

    public List<AuthorizedUrlPermission> getAuthorizedUrlPermissions() {
        return authorizedUrlPermissions;
    }

    public String getAdminSuccessUrl() {
        return adminSuccessUrl;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public int getMaxAuthFailureCount() {
        return maxAuthFailureCount;
    }

    public int getAuthFailureBlockTime() {
        return authFailureBlockTime;
    }

    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

    public boolean isRegistrationAllowed() {
        return registrationAllowed;
    }

    public String getLoginTitle() {
        return loginTitle;
    }

    public Pattern getPasswordPattern() {
        return umConfigurer.getPasswordPattern();
    }

    public String getPasswordDoesntMatchPatternMessage() {
        return umConfigurer.getPasswordDoesntMatchPatternMessage();
    }

    public boolean isCsrfEnabled() {
        return csrfEnabled;
    }

    private void updatePermission(String url, Permission permission) {
        int index = getPermissionIndex(url);
        String httpMethod = getPermissionHttpMethod(url);
        if (index != -1) {
            authorizedUrlPermissions.set(index, new AuthorizedUrlPermission(url, httpMethod, permission));
        }
    }

    private void updatePermission(String url, String httpMethod, Permission permission) {
        int index = getPermissionIndex(url);
        if (index != -1) {
            AuthorizedUrlPermission urlPermission = authorizedUrlPermissions.get(index);
            if (urlPermission != null && urlPermission.getHttpMethod().equals(httpMethod)) {
                authorizedUrlPermissions.set(index, new AuthorizedUrlPermission(url, httpMethod, permission));
            }
            else {
                authorizedUrlPermissions.add(new AuthorizedUrlPermission(url, httpMethod, permission));
            }
        }
    }

    private int getPermissionIndex(String url) {
        for (int i = 0; i < authorizedUrlPermissions.size(); i++) {
            AuthorizedUrlPermission authorizedUrlPermission = authorizedUrlPermissions.get(i);
            if (authorizedUrlPermission.getUrl().equals(url)) {
                return i;
            }
        }
        return -1;
    }

    private String getPermissionHttpMethod(String url) {
        for (AuthorizedUrlPermission authorizedUrlPermission : authorizedUrlPermissions) {
            if (authorizedUrlPermission.getUrl().equals(url)) {
                return authorizedUrlPermission.getHttpMethod();
            }
        }
        return AuthorizedUrlPermission.getDefaultHttpMethod();
    }

    private void initDefaultPermissions() {
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/js/**", Permission.ANY_USER));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/css/**", Permission.ANY_USER));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/um/users/passwordRecovery", Permission.ANY_USER));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/um/users/perform-password-recovery",
                HttpMethod.POST.name(), Permission.ANY_USER));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission(REGISTRATION_URL, Permission.ANY_USER));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission(PERFORM_REGISTER_URL, Permission.ANY_USER));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/um/users/confirmUser", Permission.ANY_USER));

        authorizedUrlPermissions
            .add(new AuthorizedUrlPermission(PERFORM_REGISTER_URL, HttpMethod.POST.name(), Permission.ANY_USER));

        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/um/security/key", Permission.ANY_USER));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/um/users/perform-password-recovery",
                HttpMethod.POST.name(), Permission.ANY_USER));

        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/um/languages", Permission.ANY_USER));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/flags/**", Permission.ANY_USER));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/img/**", Permission.ANY_USER));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/oauth/**", Permission.ANY_USER));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/login/oauth2/code/google", Permission.ANY_USER));

        authorizedUrlPermissions.add(new AuthorizedUrlPermission(LOGIN_URL, Permission.ANY_USER));

        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/um/admin**", Permission.ADMIN_ONLY));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/um/admin/**", Permission.ADMIN_ONLY));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/um/events/**", Permission.ADMIN_ONLY));

        authorizedUrlPermissions.add(new AuthorizedUrlPermission(ADMIN_PATH, Permission.ADMIN_ONLY));
        authorizedUrlPermissions
            .add(new AuthorizedUrlPermission(ADMIN_PATH, HttpMethod.PATCH.name(), Permission.ADMIN_ONLY));
        authorizedUrlPermissions
            .add(new AuthorizedUrlPermission(ADMIN_PATH, HttpMethod.POST.name(), Permission.ADMIN_ONLY));
        authorizedUrlPermissions
            .add(new AuthorizedUrlPermission(ADMIN_PATH, HttpMethod.DELETE.name(), Permission.ADMIN_ONLY));

        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/um/users/**", Permission.USER_ADMIN));

        authorizedUrlPermissions.add(new AuthorizedUrlPermission(LOGOUT_URL, Permission.USER_ADMIN));
        authorizedUrlPermissions.add(new AuthorizedUrlPermission(PERFORM_LOGOUT_URL, Permission.USER_ADMIN));

        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/um/users/changePassword", HttpMethod.PATCH.name(),
                Permission.USER_ADMIN));

        authorizedUrlPermissions
            .add(new AuthorizedUrlPermission("/um/users", HttpMethod.PATCH.name(), Permission.USER_ADMIN));

        authorizedUrlPermissions.add(new AuthorizedUrlPermission("/*", Permission.USER_ADMIN));
    }

}
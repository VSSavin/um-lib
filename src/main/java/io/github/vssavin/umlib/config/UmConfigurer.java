package io.github.vssavin.umlib.config;

/**
 * @author vssavin on 26.06.2023
 */
public class UmConfigurer {
    private String loginUrl = UmConfig.LOGIN_URL;
    private String loginProcessingUrl = UmConfig.LOGIN_PROCESSING_URL;
    private String logoutUrl = UmConfig.LOGOUT_URL;
    private String successUrl = "/index.html";
    private String adminSuccessUrl ="/um/admin";

    public UmConfigurer loginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
        return this;
    }

    public UmConfigurer loginProcessingUrl(String loginProcessingUrl) {
        this.loginProcessingUrl = loginProcessingUrl;
        return this;
    }

    public UmConfigurer logoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
        return this;
    }

    public UmConfigurer successUrl(String successUrl) {
        this.successUrl = successUrl;
        return this;
    }

    public UmConfigurer adminSuccessUrl(String adminSuccessUrl) {
        this.adminSuccessUrl = adminSuccessUrl;
        return this;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public String getLoginProcessingUrl() {
        return loginProcessingUrl;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public String getAdminSuccessUrl() {
        return adminSuccessUrl;
    }
}

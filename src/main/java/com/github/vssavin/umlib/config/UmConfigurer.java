package com.github.vssavin.umlib.config;

import com.github.vssavin.umlib.security.SecureService;

import java.util.regex.Pattern;

/**
 * @author vssavin on 26.06.2023
 */
public class UmConfigurer {
    private String loginUrl = UmConfig.LOGIN_URL;
    private String loginProcessingUrl = UmConfig.LOGIN_PROCESSING_URL;
    private String logoutUrl = UmConfig.LOGOUT_URL;
    private String successUrl = "/index.html";
    private String adminSuccessUrl ="/um/admin";
    private SecureService secureService = SecureService.defaultSecureService();
    private Pattern passwordPattern;
    private PasswordConfig passwordConfig;
    private String passwordDoesntMatchPatternMessage = "Wrong password!";

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

    public UmConfigurer secureService(SecureService secureService) {
        this.secureService = secureService;
        return this;
    }

    public UmConfigurer passwordDoesnotMatchPatternMessage(String passwordDoesntMatchPatternMessage) {
        this.passwordDoesntMatchPatternMessage = passwordDoesntMatchPatternMessage;
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

    public SecureService getSecureService() {
        return secureService;
    }

    public Pattern getPasswordPattern() {
        if (passwordPattern == null) {
            if (passwordConfig == null) passwordConfig = new PasswordConfig();
            passwordPattern = initPasswordPattern(passwordConfig);
        }
        return passwordPattern;
    }

    public String getPasswordDoesntMatchPatternMessage() {
        return passwordDoesntMatchPatternMessage;
    }

    public PasswordConfig passwordConfig() {
        this.passwordConfig = new PasswordConfig();
        return this.passwordConfig;
    }

    public static class PasswordConfig {
        private int minLength = 4;
        private int maxLength = 0;
        private boolean atLeastOneDigit = false;
        private boolean atLeastOneLowerCaseLatin = false;
        private boolean atLeastOneUpperCaseLatin = false;
        private boolean atLeastOneSpecialCharacter = false;

        public PasswordConfig minLength(int minLength) {
            this.minLength = minLength;
            return this;
        }

        public PasswordConfig maxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public PasswordConfig atLeastOneDigit(boolean atLeastOneDigit) {
            this.atLeastOneDigit = atLeastOneDigit;
            return this;
        }

        public PasswordConfig atLeastOneLowerCaseLatin(boolean atLeastOneLowerCaseLatin) {
            this.atLeastOneLowerCaseLatin = atLeastOneLowerCaseLatin;
            return this;
        }

        public PasswordConfig atLeastOneUpperCaseLatin(boolean atLeastOneUpperCaseLatin) {
            this.atLeastOneUpperCaseLatin = atLeastOneUpperCaseLatin;
            return this;
        }

        public PasswordConfig atLeastOneSpecialCharacter(boolean atLeastOneSpecialCharacter) {
            this.atLeastOneSpecialCharacter = atLeastOneSpecialCharacter;
            return this;
        }

        public int getMinLength() {
            return minLength;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public boolean isAtLeastOneDigit() {
            return atLeastOneDigit;
        }

        public boolean isAtLeastOneLowerCaseLatin() {
            return atLeastOneLowerCaseLatin;
        }

        public boolean isAtLeastOneUpperCaseLatin() {
            return atLeastOneUpperCaseLatin;
        }

        public boolean isAtLeastOneSpecialCharacter() {
            return atLeastOneSpecialCharacter;
        }

        @Override
        public String toString() {
            return "PasswordConfig{" +
                    "minLength=" + minLength +
                    ", maxLength=" + maxLength +
                    ", atLeastOneDigit=" + atLeastOneDigit +
                    ", atLeastOneLowerCaseLatin=" + atLeastOneLowerCaseLatin +
                    ", atLeastOneUpperCaseLatin=" + atLeastOneUpperCaseLatin +
                    ", atLeastOneSpecialCharacter=" + atLeastOneSpecialCharacter +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "UmConfigurer{" +
                "loginUrl='" + loginUrl + '\'' +
                ", loginProcessingUrl='" + loginProcessingUrl + '\'' +
                ", logoutUrl='" + logoutUrl + '\'' +
                ", successUrl='" + successUrl + '\'' +
                ", adminSuccessUrl='" + adminSuccessUrl + '\'' +
                ", secureService=" + secureService +
                ", passwordPattern=" + passwordPattern +
                ", passwordConfig=" + passwordConfig +
                '}';
    }

    private Pattern initPasswordPattern(PasswordConfig passwordConfig) {
        StringBuilder stringPatternBuilder = new StringBuilder("^");
        if (passwordConfig.isAtLeastOneDigit()) stringPatternBuilder.append("(?=.*[0-9])");
        if (passwordConfig.isAtLeastOneLowerCaseLatin()) stringPatternBuilder.append("(?=.*[a-z])");
        if (passwordConfig.isAtLeastOneUpperCaseLatin()) stringPatternBuilder.append("(?=.*[A-Z])");
        if (passwordConfig.isAtLeastOneSpecialCharacter())
            stringPatternBuilder.append("(?=.*[!@#&()–[{}]:;',?/*~$^+=<>])");
        stringPatternBuilder.append(".").append("{").append(passwordConfig.getMinLength()).append(",");
        if (passwordConfig.getMaxLength() != 0) stringPatternBuilder.append(passwordConfig.getMaxLength());
        stringPatternBuilder.append("}$");

        return Pattern.compile(stringPatternBuilder.toString());
    }
}

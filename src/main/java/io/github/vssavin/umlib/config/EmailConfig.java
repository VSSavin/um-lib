package io.github.vssavin.umlib.config;

import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author vssavin on 13.01.22
 */
@Component
@PropertySource(value = "file:./" + EmailConfig.CONFIG_FILE, ignoreResourceNotFound = true)
public class EmailConfig extends StorableConfig{
    @IgnoreField public static final String CONFIG_FILE = "mail.properties" ;
    @IgnoreField public static final String NAME_PREFIX = "mail";

    private String host;
    private int port;
    private String userName;
    private String password;
    private String protocol;
    private int smtpPort;
    private boolean smtpAuth;
    private boolean tlsEnabled;
    private boolean tlsRequired;

    public EmailConfig() {
        super.setConfigFile(CONFIG_FILE);
        super.setNamePrefix(NAME_PREFIX);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getProtocol() {
        return protocol;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public boolean isSmtpAuth() {
        return smtpAuth;
    }

    public boolean isTlsEnabled() {
        return tlsEnabled;
    }

    public boolean isTlsRequired() {
        return tlsRequired;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public void setSmtpAuth(boolean smtpAuth) {
        this.smtpAuth = smtpAuth;
    }

    public void setTlsEnabled(boolean tlsEnabled) {
        this.tlsEnabled = tlsEnabled;
    }

    public void setTlsRequired(boolean tlsRequired) {
        this.tlsRequired = tlsRequired;
    }
}

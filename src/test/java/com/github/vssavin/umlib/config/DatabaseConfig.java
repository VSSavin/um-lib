package com.github.vssavin.umlib.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author vssavin on 17.12.21
 */

@Component
@ConfigurationProperties(prefix = DatabaseConfig.NAME_PREFIX)
@PropertySource("file:./" + DatabaseConfig.CONFIG_FILE)
public class DatabaseConfig extends StorableConfig {
    @IgnoreField public static final String CONFIG_FILE = "conf.properties";
    @IgnoreField public static final String NAME_PREFIX = "db";

    @IgnoreField private String url;

    private String driverClass;
    private String dialect;
    private String name;
    private String user;
    private String password;

    public DatabaseConfig() {
        super.setConfigFile(CONFIG_FILE);
        super.setNamePrefix(NAME_PREFIX);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

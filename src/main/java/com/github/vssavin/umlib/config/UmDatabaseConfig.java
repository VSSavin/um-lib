package com.github.vssavin.umlib.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration of user management database params.
 *
 * @author vssavin on 25.08.2022.
 */
@Configuration
@PropertySource(value = "classpath:" + UmDatabaseConfig.CONFIG_FILE)
public class UmDatabaseConfig {
    static final String CONFIG_FILE = "um_db_conf.properties";

    @Value("${um.db.url}")
    private String url;

    @Value("${um.db.driverClass}")
    private String driverClass;

    @Value("${um.db.dialect}")
    private String dialect;

    @Value("${um.db.name}")
    private String name;

    @Value("${um.db.user}")
    private String user;

    @Value("${um.db.password}")
    private String password;

    @Value("${um.db.additionalParams}")
    private String additionalParams;

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

    public String getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(String additionalParams) {
        this.additionalParams = additionalParams;
    }
}

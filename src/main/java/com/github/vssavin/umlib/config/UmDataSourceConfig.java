package com.github.vssavin.umlib.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;

/**
 * Configuration of user management data sources.
 *
 * @author vssavin on 25.08.2022.
 */
public class UmDataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(UmDataSourceConfig.class);

    private final UmDatabaseConfig umDatabaseConfig;

    private final UmPasswordEncodingArgumentsHandler argumentsHandler;

    private DataSource umDataSource;

    public UmDataSourceConfig(UmDatabaseConfig umDatabaseConfig, UmPasswordEncodingArgumentsHandler argumentsHandler) {
        this.umDatabaseConfig = umDatabaseConfig;
        this.argumentsHandler = argumentsHandler;
    }

    @Bean
    protected DataSource umDataSource() {
        if (this.umDataSource != null) {
            return this.umDataSource;
        }
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        try {
            dataSource.setDriverClassName(umDatabaseConfig.getDriverClass());
            String url = umDatabaseConfig.getUrl() + "/" + umDatabaseConfig.getName();
            if (umDatabaseConfig.getDriverClass().equals("org.h2.Driver")) {
                url += ";" + umDatabaseConfig.getAdditionalParams();
            }
            dataSource.setUrl(url);
            dataSource.setUsername(umDatabaseConfig.getUser());
            setDatasourcePassword(dataSource, argumentsHandler);
        }
        catch (Exception e) {
            log.error("Creating datasource error: ", e);
        }
        this.umDataSource = dataSource;

        return dataSource;
    }

    @Bean
    AbstractRoutingDataSource routingDataSource(
            @Autowired(required = false) @Qualifier("appDataSource") DataSource appDataSource,
            @Autowired(required = false) @Qualifier("dataSource") DataSource dataSource,
            @Autowired DataSource umDataSource) {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        DataSource ds = appDataSource != null ? appDataSource : dataSource;
        routingDataSource.addDataSource(RoutingDataSource.DATASOURCE_TYPE.UM_DATASOURCE, umDataSource);
        if (ds == null) {
            routingDataSource.setKey(RoutingDataSource.DATASOURCE_TYPE.UM_DATASOURCE);
            routingDataSource.setDefaultTargetDataSource(umDataSource);
        }
        else {
            routingDataSource.addDataSource(RoutingDataSource.DATASOURCE_TYPE.APPLICATION_DATASOURCE, ds);
            routingDataSource.setDefaultTargetDataSource(ds);
        }

        return routingDataSource;
    }

    private void setDatasourcePassword(DriverManagerDataSource dataSource,
                                       UmPasswordEncodingArgumentsHandler argumentsHandler) {
        if (argumentsHandler.isDbPasswordEncoded()) {
            try {
                dataSource
                        .setPassword(argumentsHandler.getPasswordService().decrypt(umDatabaseConfig.getPassword()));
            }
            catch (Exception e) {
                log.debug("Can't decrypt password! Using a password from the config...", e);
                dataSource.setPassword(umDatabaseConfig.getPassword());
            }
        }
        else {
            dataSource.setPassword(umDatabaseConfig.getPassword());
        }
    }

}

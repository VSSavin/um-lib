package io.github.vssavin.umlib.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;

import static io.github.vssavin.umlib.config.RoutingDataSource.DATASOURCE_TYPE.APPLICATION_DATASOURCE;
import static io.github.vssavin.umlib.config.RoutingDataSource.DATASOURCE_TYPE.UM_DATASOURCE;

/**
 * Created by vssavin on 25.08.2022.
 */
@Configuration
class UmDataSourceConfig {
    private static final Logger log = LoggerFactory.getLogger(UmDataSourceConfig.class);

    private final UmDatabaseConfig umDatabaseConfig;
    private final DataSource applicationDataSource;
    private DataSource umDataSource;

    public UmDataSourceConfig(UmDatabaseConfig umDatabaseConfig, DataSource applicationDataSource) {
        this.umDatabaseConfig = umDatabaseConfig;
        this.applicationDataSource = applicationDataSource;
    }

    @Bean
    protected DataSource umDataSource(){
        if (this.umDataSource != null) return this.umDataSource;
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        try {
            dataSource.setDriverClassName(umDatabaseConfig.getDriverClass());
            String url = umDatabaseConfig.getUrl() + "/"
                    + umDatabaseConfig.getName();
            if (umDatabaseConfig.getDriverClass().equals("org.h2.Driver")) {
                url += ";" + umDatabaseConfig.getAdditionalParams();
            }
            dataSource.setUrl(url);
            dataSource.setUsername(umDatabaseConfig.getUser());
            dataSource.setPassword(umDatabaseConfig.getPassword());
        } catch (Exception e) {
            log.error("Creating datasource error: ", e);
        }
        this.umDataSource = dataSource;

        return dataSource;
    }

    @Bean
    AbstractRoutingDataSource routingDataSource(){
        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.addDataSource(UM_DATASOURCE, umDataSource());
        routingDataSource.addDataSource(APPLICATION_DATASOURCE, applicationDataSource);
        routingDataSource.setDefaultTargetDataSource(umDataSource);
        return routingDataSource;
    }
}

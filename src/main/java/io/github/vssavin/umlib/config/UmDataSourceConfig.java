package io.github.vssavin.umlib.config;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.H2Templates;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLTemplates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;


/**
 * Created by vssavin on 25.08.2022.
 */
public class UmDataSourceConfig {
    private static final Logger log = LoggerFactory.getLogger(UmDataSourceConfig.class);

    private final UmDatabaseConfig umDatabaseConfig;
    private DataSource umDataSource;

    public UmDataSourceConfig(UmDatabaseConfig umDatabaseConfig) {
        this.umDatabaseConfig = umDatabaseConfig;
    }

    @Bean
    protected DataSource umDataSource() {
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
    protected Configuration queryDslConfiguration() {
        String driverClass = umDatabaseConfig.getDriverClass();

        if (driverClass.contains("h2")) {
            return new Configuration(new H2Templates());
            //return new Configuration(new H2Templates(true));
        } else if (driverClass.contains("postgres")) {
            return new Configuration(new PostgreSQLTemplates());
        }
        throw new IllegalArgumentException("Database driver: " + driverClass + " unsupported yet!");
    }

    @Bean
    AbstractRoutingDataSource routingDataSource(@Autowired(required = false)
                                                @Qualifier("appDataSource") DataSource appDataSource,
                                                @Autowired(required = false)
                                                @Qualifier("dataSource") DataSource dataSource,
                                                @Autowired DataSource umDataSource) {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        DataSource ds = appDataSource != null ? appDataSource : dataSource;
        routingDataSource.addDataSource(RoutingDataSource.DATASOURCE_TYPE.UM_DATASOURCE, umDataSource);
        if (ds != null) {
            routingDataSource.addDataSource(RoutingDataSource.DATASOURCE_TYPE.APPLICATION_DATASOURCE, ds);
            routingDataSource.setDefaultTargetDataSource(ds);
        }
        if (ds == null) {
            routingDataSource.setKey(RoutingDataSource.DATASOURCE_TYPE.UM_DATASOURCE);
            routingDataSource.setDefaultTargetDataSource(umDataSource);
        }
        return routingDataSource;
    }
}

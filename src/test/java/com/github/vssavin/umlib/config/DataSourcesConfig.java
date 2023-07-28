package com.github.vssavin.umlib.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

/**
 * @author vssavin on 25.08.2022.
 */
@Configuration
@Import(UmDataSourceConfig.class)
public class DataSourcesConfig {
    private DataSource appDataSource;

    @Bean
    public DataSource dataSource() {
        if (appDataSource == null) {
            appDataSource = new EmbeddedDatabaseBuilder()
                    .generateUniqueName(true)
                    .setType(H2)
                    .setScriptEncoding("UTF-8")
                    .ignoreFailedDrops(true)
                    .addScript("init.sql")
                    .build();
        }
        return appDataSource;
    }

    @Bean
    public DataSource appDataSource() {
        return dataSource();
    }

    @Bean("umDataSource")
    @Profile("um-test")
    protected DataSource umDataSourceTest() {
        return dataSource();
    }


    @Bean("routingDataSource")
    @Profile("um-test")
    @Primary
    AbstractRoutingDataSource routingDataSource(@Autowired(required = false)
                                                @Qualifier("appDataSource") DataSource appDataSource,
                                                @Autowired(required = false)
                                                @Qualifier("dataSource") DataSource dataSource,
                                                @Autowired DataSource umDataSource) {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        DataSource ds = appDataSource != null ? appDataSource : dataSource;
        routingDataSource.addDataSource(RoutingDataSource.DATASOURCE_TYPE.UM_DATASOURCE, umDataSource);
        if (ds == null) {
            routingDataSource.setKey(RoutingDataSource.DATASOURCE_TYPE.UM_DATASOURCE);
            routingDataSource.setDefaultTargetDataSource(umDataSource);
        } else {
            routingDataSource.addDataSource(RoutingDataSource.DATASOURCE_TYPE.APPLICATION_DATASOURCE, ds);
            routingDataSource.setDefaultTargetDataSource(ds);
        }

        return new SettableUmRoutingDatasource(routingDataSource);
    }

}

package io.github.vssavin.umlib.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import javax.sql.DataSource;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

/**
 * Created by vssavin on 25.08.2022.
 */
@Configuration
public class DataSourcesConfig extends UmDataSourceConfig{
    private DataSource appDataSource;

    public DataSourcesConfig(UmDatabaseConfig umDatabaseConfig) {
        super(umDatabaseConfig);
    }

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
}

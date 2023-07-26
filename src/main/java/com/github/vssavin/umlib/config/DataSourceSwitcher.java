package com.github.vssavin.umlib.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Wrapper to provide switching between user management datasource and main datasource.
 *
 * Created by vssavin on 25.08.2022.
 */
@Component
public class DataSourceSwitcher {

    private final AbstractRoutingDataSource routingDataSource;
    private RoutingDataSource.DATASOURCE_TYPE previousDataSourceKey;

    public DataSourceSwitcher(AbstractRoutingDataSource routingDataSource) {
        this.routingDataSource = routingDataSource;
        this.previousDataSourceKey = RoutingDataSource.DATASOURCE_TYPE.APPLICATION_DATASOURCE;
    }

    public void switchToUmDataSource() {
        previousDataSourceKey = ((RoutingDataSource) routingDataSource).getDatasourceKey();
        ((RoutingDataSource) routingDataSource).setKey(RoutingDataSource.DATASOURCE_TYPE.UM_DATASOURCE);
    }

    public void switchToApplicationDataSource() {
        previousDataSourceKey = ((RoutingDataSource) routingDataSource).getDatasourceKey();
        ((RoutingDataSource) routingDataSource).setKey(RoutingDataSource.DATASOURCE_TYPE.APPLICATION_DATASOURCE);
    }

    public void switchToPreviousDataSource() {
        ((RoutingDataSource) routingDataSource).setKey(previousDataSourceKey);
    }

    public DataSource getCurrentDataSource() {
        return ((RoutingDataSource) routingDataSource).determineTargetDataSource();
    }
}

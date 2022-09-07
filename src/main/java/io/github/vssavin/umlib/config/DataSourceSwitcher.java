package io.github.vssavin.umlib.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static io.github.vssavin.umlib.config.RoutingDataSource.DATASOURCE_TYPE;
import static io.github.vssavin.umlib.config.RoutingDataSource.DATASOURCE_TYPE.UM_DATASOURCE;
import static io.github.vssavin.umlib.config.RoutingDataSource.DATASOURCE_TYPE.APPLICATION_DATASOURCE;

/**
 * Created by vssavin on 25.08.2022.
 */
@Component
public class DataSourceSwitcher {

    private final AbstractRoutingDataSource routingDataSource;
    private DATASOURCE_TYPE previousDataSourceKey;

    public DataSourceSwitcher(AbstractRoutingDataSource routingDataSource) {
        this.routingDataSource = routingDataSource;
        this.previousDataSourceKey = APPLICATION_DATASOURCE;
    }

    public void switchToUmDataSource() {
        previousDataSourceKey = ((RoutingDataSource)routingDataSource).getDatasourceKey();
        ((RoutingDataSource)routingDataSource).setKey(UM_DATASOURCE);
    }

    public void switchToApplicationDataSource() {
        previousDataSourceKey = ((RoutingDataSource)routingDataSource).getDatasourceKey();
        ((RoutingDataSource)routingDataSource).setKey(APPLICATION_DATASOURCE);
        DataSource ds = ((RoutingDataSource)routingDataSource).determineTargetDataSource();
        if (ds == null) ((RoutingDataSource)routingDataSource).setKey(previousDataSourceKey);
    }

    public void switchToPreviousDataSource() {
        DATASOURCE_TYPE currentKey = ((RoutingDataSource)routingDataSource).getDatasourceKey();
        ((RoutingDataSource)routingDataSource).setKey(previousDataSourceKey);
        DataSource ds = ((RoutingDataSource)routingDataSource).determineTargetDataSource();
        if (ds == null) ((RoutingDataSource)routingDataSource).setKey(currentKey);
    }
}

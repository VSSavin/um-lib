package com.github.vssavin.umlib.config;

import javax.sql.DataSource;

/**
 * Routing datasource that provides manually set user management datasource.
 *
 * @author vssavin on 27.07.2023
 */
public class SettableUmRoutingDatasource extends RoutingDataSource {

    public SettableUmRoutingDatasource(RoutingDataSource routingDataSource) {
        super.getDataSources().putAll(routingDataSource.getDataSources());
    }

    public void setUmDataSource(DataSource dataSource) {
        super.addDataSource(DATASOURCE_TYPE.UM_DATASOURCE, dataSource);
    }
}

package com.github.vssavin.umlib.data.querydsl;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;

import javax.inject.Provider;
import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @author vssavin on 19.06.2023
 */
public class CustomH2QueryFactory extends SQLQueryFactory {
    public CustomH2QueryFactory(SQLTemplates templates, Provider<Connection> connection) {
        super(templates, connection);
    }

    public CustomH2QueryFactory(Configuration configuration, Provider<Connection> connProvider) {
        super(configuration, connProvider);
    }

    public CustomH2QueryFactory(Configuration configuration, DataSource dataSource) {
        super(configuration, dataSource);
    }

    public CustomH2QueryFactory(Configuration configuration, DataSource dataSource, boolean release) {
        super(configuration, dataSource, release);
    }

    public H2SQLUpdateClause h2Update(RelationalPath<?> path) {
        return new H2SQLUpdateClause(connection, configuration, path);
    }
}

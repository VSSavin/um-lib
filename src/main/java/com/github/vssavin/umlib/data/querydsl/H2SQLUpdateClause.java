package com.github.vssavin.umlib.data.querydsl;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.SQLUpdateClause;

import javax.inject.Provider;
import java.sql.Connection;

/**
 * @author vssavin on 19.06.2023
 */
public class H2SQLUpdateClause extends SQLUpdateClause {
    public H2SQLUpdateClause(Connection connection, SQLTemplates templates, RelationalPath<?> entity) {
        super(connection, templates, entity);
    }

    public H2SQLUpdateClause(Connection connection, Configuration configuration, RelationalPath<?> entity) {
        super(connection, configuration, entity);
    }

    public H2SQLUpdateClause(Provider<Connection> connection, Configuration configuration, RelationalPath<?> entity) {
        super(connection, configuration, entity);
    }

    @Override
    protected SQLSerializer createSerializer() {
        return new H2SqlSerializer(configuration, true);
    }
}

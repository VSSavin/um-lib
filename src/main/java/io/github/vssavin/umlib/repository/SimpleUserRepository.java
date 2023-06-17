package io.github.vssavin.umlib.repository;

import com.querydsl.core.types.*;
import com.querydsl.sql.*;
import com.querydsl.sql.postgresql.PostgreSQLQueryFactory;
import io.github.vssavin.umlib.config.DataSourceSwitcher;
import io.github.vssavin.umlib.entity.QUser;
import io.github.vssavin.umlib.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import javax.inject.Provider;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.querydsl.core.types.Projections.bean;

/**
 * @author vssavin on 16.06.2023
 */
@Repository
public class SimpleUserRepository implements UserRepository {

    private static final QUser users = new QUser("users");
    private static final QBean<User> userBean = bean(User.class, users.id, users.login, users.name, users.password, users.email,
            users.authority, users.expiration_date, users.verification_id);

    private final DataSourceSwitcher dataSourceSwitcher;
    private final Configuration queryDslConfiguration;

    private AbstractSQLQueryFactory<?> queryFactory;

    @Autowired
    public SimpleUserRepository(DataSourceSwitcher dataSourceSwitcher, Configuration queryDslConfiguration) {
        this.dataSourceSwitcher = dataSourceSwitcher;
        this.queryDslConfiguration = queryDslConfiguration;
    }

    @Override
    public List<User> findByLogin(String login) {
        return prepareQuery(false).where(users.login.eq(login)).fetch();
    }

    @Override
    public List<User> findUserByName(String name) {
        return new ArrayList<>();//TODO: implement this
    }

    @Override
    public List<User> findByEmail(String email) {
        return new ArrayList<>();//TODO: implement this
    }

    @Override
    public void deleteByLogin(String login) {
        //TODO: implement this
    }

    @Override
    public Optional<User> findOne(Predicate predicate) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public Iterable<User> findAll(Predicate predicate) {
        return new ArrayList<>();//TODO: implement this
    }

    @Override
    public Iterable<User> findAll(Predicate predicate, Sort sort) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public Iterable<User> findAll(Predicate predicate, OrderSpecifier<?>... orders) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public Iterable<User> findAll(OrderSpecifier<?>... orders) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        long totalCount = prepareQuery(false).fetchCount();
        return new PageImpl<>(prepareQuery(true)
                .limit(pageable.getPageSize()).offset(pageable.getOffset()).fetch(), pageable,
                totalCount);
    }

    @Override
    public Page<User> findAll(Predicate predicate, Pageable pageable) {
        long totalCount = prepareQuery(false).where(predicate).fetchCount();
        return new PageImpl<>(prepareQuery(true).where(predicate)
                .limit(pageable.getPageSize()).offset(pageable.getOffset()).fetch(), pageable, totalCount);
    }

    @Override
    public long count(Predicate predicate) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public boolean exists(Predicate predicate) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }


    @Override
    public <S extends User, R> R findBy(Predicate predicate,
                                        Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public Iterable<User> findAll(Sort sort) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public <S extends User> S save(S entity) {
        //Check if entity exists or not exists
        return entity;  //TODO: implement this
    }

    @Override
    public <S extends User> Iterable<S> saveAll(Iterable<S> entities) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public Optional<User> findById(Long id) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public boolean existsById(Long id) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public Iterable<User> findAll() {
        return new ArrayList<>();//TODO: implement this
    }

    @Override
    public Iterable<User> findAllById(Iterable<Long> ids) {
        return new ArrayList<>();//TODO: implement this
    }

    @Override
    public long count() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public void deleteById(Long aLong) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public void delete(User entity) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public void deleteAll(Iterable<? extends User> entities) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    private AbstractSQLQuery<User,?> prepareQuery(boolean useLastQueryFactory) {
        if (useLastQueryFactory && queryFactory != null) {
            return queryFactory.select(userBean).from(users);
        }
        DataSource dataSource = dataSourceSwitcher.getCurrentDataSource();
        if (queryDslConfiguration.getTemplates() instanceof PostgreSQLTemplates) {
            queryFactory = new PostgreSQLQueryFactory(queryDslConfiguration, new DataSourceProvider(dataSource));
        }
        else queryFactory = new SQLQueryFactory(queryDslConfiguration, dataSource);
        return queryFactory.select(userBean).from(users);
    }

    private static class DataSourceProvider implements Provider<Connection> {

        private final DataSource ds;

        public DataSourceProvider(DataSource ds) {
            this.ds = ds;
        }

        @Override
        public Connection get() {
            try {
                return ds.getConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

    }
}

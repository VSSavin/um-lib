package io.github.vssavin.umlib.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.*;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import com.querydsl.sql.postgresql.PostgreSQLQueryFactory;
import io.github.vssavin.umlib.config.DataSourceSwitcher;
import io.github.vssavin.umlib.data.querydsl.CustomH2QueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import javax.inject.Provider;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;

import static com.querydsl.core.types.Projections.bean;

/**
 * @author vssavin on 16.06.2023
 */
@Repository
public class SimpleUserRepository implements UserRepository {

    private static final QUser users = new QUser("users");

    private static final QBean<User> userBean = bean(User.class, users.id, users.login, users.name, users.password,
            users.email, users.authority, users.expiration_date, users.verification_id);

    private static final RelationalPathBase<User> usersRelationalPath =
            new RelationalPathBase<>(users.getType(), users.getMetadata(), "", "users");

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
        return prepareSelectQuery(false).where(users.login.eq(login)).fetch();
    }

    @Override
    public List<User> findUserByName(String name) {
        return prepareSelectQuery(false).where(users.name.eq(name)).fetch();
    }

    @Override
    public List<User> findByEmail(String email) {
        return prepareSelectQuery(false).where(users.email.eq(email)).fetch();
    }

    @Override
    public void deleteByLogin(String login) {
        prepareDeleteQuery().where(users.login.eq(login)).execute();
    }

    @NonNull
    @Override
    public Optional<User> findOne(@NonNull Predicate predicate) {
        return Optional.of(prepareSelectQuery(false).where(predicate).fetchOne());
    }

    @NonNull
    @Override
    public Iterable<User> findAll(@NonNull Predicate predicate) {
        return prepareSelectQuery(false).where(predicate).fetch();
    }

    @NonNull
    @Override
    public Iterable<User> findAll(@NonNull Predicate predicate, @NonNull Sort sort) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @NonNull
    @Override
    public Iterable<User> findAll(@NonNull Predicate predicate, @NonNull OrderSpecifier<?>... orders) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @NonNull
    @Override
    public Iterable<User> findAll(@NonNull OrderSpecifier<?>... orders) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @NonNull
    @Override
    public Page<User> findAll(@NonNull Pageable pageable) {
        long totalCount = prepareSelectQuery(false).fetchCount();
        return new PageImpl<>(prepareSelectQuery(true)
                .limit(pageable.getPageSize()).offset(pageable.getOffset()).fetch(), pageable,
                totalCount);
    }

    @NonNull
    @Override
    public Page<User> findAll(@NonNull Predicate predicate, @NonNull Pageable pageable) {
        long totalCount = prepareSelectQuery(false).where(predicate).fetchCount();
        return new PageImpl<>(prepareSelectQuery(true).where(predicate)
                .limit(pageable.getPageSize()).offset(pageable.getOffset()).fetch(), pageable, totalCount);
    }

    @Override
    public long count(@NonNull Predicate predicate) {
        return prepareSelectQuery(false).where(predicate).fetchCount();
    }

    @Override
    public boolean exists(@NonNull Predicate predicate) {
        return prepareSelectQuery(false).where(predicate).fetchCount() > 0;
    }

    @NonNull
    @Override
    public <S extends User, R> R findBy(@NonNull Predicate predicate,
                                        @NonNull Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @NonNull
    @Override
    public Iterable<User> findAll(@NonNull Sort sort) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @NonNull
    @Override
    public <S extends User> S save(S entity) {
        User entityFromDatabase;
        if (entity.getId() != null) {
            entityFromDatabase = prepareSelectQuery(false).where(users.id.eq(entity.getId())).fetchOne();
        } else {
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(users.login.eq(entity.getLogin())).and(users.email.eq(entity.getEmail()))
                    .and(users.name.eq(entity.getName())).and(users.password.eq(entity.getPassword()));
            entityFromDatabase = prepareSelectQuery(false).where(builder).fetchOne();
        }

        if (entityFromDatabase != null) {
            Map<Path<?>, Path<?>> updateMap = prepareUpdateMap(entity);
            List<Path<?>> updateListFields = new ArrayList<>(updateMap.keySet());
            List<Path<?>> updateListValues = new ArrayList<>(updateMap.values());
            prepareUpdateQuery(true)
                    .where(users.id.eq(entityFromDatabase.getId()))
                    .set(updateListFields, updateListValues).execute();
        } else {
            queryFactory
                    .insert(usersRelationalPath)
                    .columns(users.login, users.name, users.password, users.email, users.authority,
                            users.expiration_date, users.verification_id)
                    .values(entity.getLogin(), entity.getName(), entity.getPassword(), entity.getEmail(),
                            entity.getAuthority(), entity.getExpirationDate(), entity.getVerificationId())
                    .execute();
        }

        return entity;
    }

    @NonNull
    @Override
    public <S extends User> Iterable<S> saveAll(@NonNull Iterable<S> entities) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @NonNull
    @Override
    public Optional<User> findById(@NonNull Long id) {
        return Optional.ofNullable(prepareSelectQuery(false).where(users.id.eq(id)).fetchOne());
    }

    @Override
    public boolean existsById(@NonNull Long id) {
        return prepareSelectQuery(false).where(users.id.eq(id)).fetchCount() > 0;
    }

    @NonNull
    @Override
    public Iterable<User> findAll() {
        return prepareSelectQuery(false).fetch();
    }

    @NonNull
    @Override
    public Iterable<User> findAllById(Iterable<Long> ids) {
        BooleanBuilder builder = new BooleanBuilder();
        ids.forEach(id -> builder.or(users.id.eq(id)));
        return prepareSelectQuery(false).where(builder).fetch();
    }

    @Override
    public long count() {
        return prepareSelectQuery(false).fetchCount();
    }

    @Override
    public void deleteById(@NonNull Long id) {
        prepareDeleteQuery().where(users.id.eq(id)).execute();
    }

    @Override
    public void delete(User entity) {
        if (entity.getId() == null) {
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(users.login.eq(entity.getLogin())).and(users.name.eq(entity.getName()))
                    .and(users.password.eq(entity.getPassword())).and(users.email.eq(entity.getEmail()));
            prepareDeleteQuery().where(builder).execute();
        } else {
            prepareDeleteQuery().where(users.id.eq(entity.getId())).execute();
        }
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        BooleanBuilder builder = new BooleanBuilder();
        ids.forEach(id -> builder.or(users.id.eq(id)));
        prepareDeleteQuery().where(builder).execute();
    }

    @Override
    public void deleteAll(@NonNull Iterable<? extends User> entities) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public void deleteAll() {
        prepareDeleteQuery().execute();
    }

    private Map<Path<?>,Path<?>> prepareUpdateMap(User entity) {
        Map<Path<?>, Path<?>> map = new HashMap<>();
        map.put(users.login, Expressions.stringPath(entity.getLogin()));
        map.put(users.name, Expressions.stringPath(entity.getName()));
        map.put(users.password, Expressions.stringPath(entity.getPassword()));
        map.put(users.email, Expressions.stringPath(entity.getEmail()));
        map.put(users.authority, Expressions.stringPath(entity.getAuthority()));
        map.put(users.expiration_date, Expressions.dateTimePath(Date.class, dateToTimestamp(entity.getExpirationDate())));
        if (entity.getVerificationId() != null) {
            map.put(users.verification_id, Expressions.stringPath(entity.getVerificationId()));
        }

        return map;
    }

    private SQLUpdateClause prepareUpdateQuery(boolean useLastQueryFactory) {
        if (!useLastQueryFactory) {
            queryFactory = initNewQueryFactory();
        }

        if (queryFactory instanceof CustomH2QueryFactory) {
            return ((CustomH2QueryFactory) queryFactory).h2Update(usersRelationalPath);

        } else {
            return queryFactory.update(usersRelationalPath);
        }
    }

    private AbstractSQLQuery<User,?> prepareSelectQuery(boolean useLastQueryFactory) {
        if (useLastQueryFactory && queryFactory != null) {
            return queryFactory.select(userBean).from(users);
        }
        queryFactory = initNewQueryFactory();
        return queryFactory.select(userBean).from(users);
    }

    private SQLDeleteClause prepareDeleteQuery() {
        AbstractSQLQueryFactory<?> queryFactory = initNewQueryFactory();
        return queryFactory.delete(new RelationalPathBase<User>(users.getType(), users.getMetadata(),
                "","users"));
    }

    private AbstractSQLQueryFactory<?> initNewQueryFactory() {
        DataSource dataSource = dataSourceSwitcher.getCurrentDataSource();
        AbstractSQLQueryFactory<?> queryFactory;
        if (queryDslConfiguration.getTemplates() instanceof PostgreSQLTemplates) {
            queryFactory = new PostgreSQLQueryFactory(queryDslConfiguration, new DataSourceProvider(dataSource));
        } else if (queryDslConfiguration.getTemplates() instanceof H2Templates) {
            queryFactory = new CustomH2QueryFactory(queryDslConfiguration, dataSource);
        }
        else queryFactory = new SQLQueryFactory(queryDslConfiguration, dataSource);

        return queryFactory;
    }

    private String dateToTimestamp(Date date) {
        return new Timestamp(date.getTime()).toString();
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

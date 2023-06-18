package io.github.vssavin.umlib.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.*;
import com.querydsl.sql.dml.SQLDeleteClause;
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
import java.util.Date;
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


    /*
    private static final QBean<User> userBean = bean(User.class,
            new RelationalPathBase<Long>(users.id.getType(), users.id.getMetadata(), "","users"),
            new RelationalPathBase<String>(users.login.getType(), users.login.getMetadata(), "","users"),
            new RelationalPathBase<String>(users.name.getType(), users.name.getMetadata(), "","users"),
            new RelationalPathBase<String>(users.password.getType(), users.password.getMetadata(), "","users"),
            new RelationalPathBase<String>(users.email.getType(), users.email.getMetadata(), "","users"),
            new RelationalPathBase<String>(users.authority.getType(), users.authority.getMetadata(), "","users"),
            new RelationalPathBase<Date>(users.expiration_date.getType(), users.expiration_date.getMetadata(), "","users"),
            new RelationalPathBase<String>(users.verification_id.getType(), users.verification_id.getMetadata(), "","users")
            );
    */

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

    @Override
    public Optional<User> findOne(Predicate predicate) {
        return Optional.of(prepareSelectQuery(false).where(predicate).fetchOne());
    }

    @Override
    public Iterable<User> findAll(Predicate predicate) {
        return prepareSelectQuery(false).where(predicate).fetch();
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
        long totalCount = prepareSelectQuery(false).fetchCount();
        return new PageImpl<>(prepareSelectQuery(true)
                .limit(pageable.getPageSize()).offset(pageable.getOffset()).fetch(), pageable,
                totalCount);
    }

    @Override
    public Page<User> findAll(Predicate predicate, Pageable pageable) {
        long totalCount = prepareSelectQuery(false).where(predicate).fetchCount();
        return new PageImpl<>(prepareSelectQuery(true).where(predicate)
                .limit(pageable.getPageSize()).offset(pageable.getOffset()).fetch(), pageable, totalCount);
    }

    @Override
    public long count(Predicate predicate) {
        return prepareSelectQuery(false).where(predicate).fetchCount();
    }

    @Override
    public boolean exists(Predicate predicate) {
        return prepareSelectQuery(false).where(predicate).fetchCount() > 0;
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
        User entityFromDatabase = null;
        if (entity.getId() != null) {
            entityFromDatabase = prepareSelectQuery(false).where(users.id.eq(entity.getId())).fetchOne();
        } else {
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(users.login.eq(entity.getLogin())).and(users.email.eq(entity.getEmail()))
                    .and(users.name.eq(entity.getName())).and(users.password.eq(entity.getPassword()));
            entityFromDatabase = prepareSelectQuery(false).where(builder).fetchOne();
        }
        if (entityFromDatabase != null) {
            List<Path<?>> updateListFields = new ArrayList<>();
            List<Path<?>> updateListValues = new ArrayList<>();
            updateListFields.add(users.login);
            updateListFields.add(users.name);
            updateListFields.add(users.password);
            updateListFields.add(users.email);
            updateListFields.add(users.authority);
            updateListFields.add(users.expiration_date);
            if (entity.getVerificationId() != null) updateListFields.add(users.verification_id);

            String loginValue = entity.getLogin();
            String nameValue = entity.getName();
            String passwordValue = entity.getPassword();
            String emailValue = entity.getEmail();
            String authorityValue = entity.getAuthority();
            String verificationIdValue = entity.getVerificationId();

            /*
            if (queryDslConfiguration.getTemplates() instanceof CustomH2Templates ||
                    queryDslConfiguration.getTemplates() instanceof H2Templates) {
                loginValue = "'" + loginValue + "'";
                nameValue = "'" + nameValue + "'";
                passwordValue = "'" + passwordValue + "'";
                emailValue = "'" + emailValue + "'";
                authorityValue = "'" + authorityValue + "'";
                if (verificationIdValue != null) verificationIdValue = "'" + verificationIdValue + "'";
            }
            */

            updateListValues.add(Expressions.stringPath(loginValue));
            updateListValues.add(Expressions.stringPath(nameValue));
            updateListValues.add(Expressions.stringPath(passwordValue));
            updateListValues.add(Expressions.stringPath(emailValue));
            updateListValues.add(Expressions.stringPath(authorityValue));
            updateListValues.add(Expressions.dateTimePath(Date.class, entity.getExpirationDate().toString()));  //TODO: test this!!!
            if (verificationIdValue!= null)
                updateListValues.add(Expressions.stringPath(verificationIdValue));

            queryFactory.update(new RelationalPathBase<User>(users.getType(), users.getMetadata(), "", "users"))
                    .where(users.id.eq(entityFromDatabase.getId()))
                    .set(updateListFields, updateListValues)
                    .execute();
        } else {

            queryFactory
                    .insert(new RelationalPathBase<User>(users.getType(), users.getMetadata(), "", "users"))
                    .columns(users.login, users.name, users.password, users.email, users.authority,
                            users.expiration_date, users.verification_id)
                    .values(entity.getLogin(), entity.getName(), entity.getPassword(), entity.getEmail(),
                            entity.getAuthority(), entity.getExpirationDate(), entity.getVerificationId())
                    .execute();
        }


        return entity;  //TODO: implement this
    }

    @Override
    public <S extends User> Iterable<S> saveAll(Iterable<S> entities) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(prepareSelectQuery(false).where(users.id.eq(id)).fetchOne());
    }

    @Override
    public boolean existsById(Long id) {
        return prepareSelectQuery(false).where(users.id.eq(id)).fetchCount() > 0;
    }

    @Override
    public Iterable<User> findAll() {
        return prepareSelectQuery(false).fetch();
    }

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
    public void deleteById(Long id) {
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
    public void deleteAll(Iterable<? extends User> entities) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public void deleteAll() {
        prepareDeleteQuery().execute();
    }

    private AbstractSQLQuery<User,?> prepareSelectQuery(boolean useLastQueryFactory) {
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

    private SQLDeleteClause prepareDeleteQuery() {
        DataSource dataSource = dataSourceSwitcher.getCurrentDataSource();
        if (queryDslConfiguration.getTemplates() instanceof PostgreSQLTemplates) {
            queryFactory = new PostgreSQLQueryFactory(queryDslConfiguration, new DataSourceProvider(dataSource));
        }
        else queryFactory = new SQLQueryFactory(queryDslConfiguration, dataSource);

        return queryFactory.delete(new RelationalPathBase<User>(users.getType(), users.getMetadata(),
                "","users"));
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

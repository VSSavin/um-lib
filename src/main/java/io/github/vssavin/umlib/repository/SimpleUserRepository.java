package io.github.vssavin.umlib.repository;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.QBean;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQueryFactory;
import io.github.vssavin.umlib.config.DataSourceSwitcher;
import io.github.vssavin.umlib.entity.QUser;
import io.github.vssavin.umlib.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.querydsl.core.types.Projections.bean;
import static com.querydsl.core.types.Projections.constructor;

/**
 * @author vssavin on 16.06.2023
 */
@Repository
public class SimpleUserRepository implements UserRepository{

    private final DataSourceSwitcher dataSourceSwitcher;
    private final Configuration queryDslConfiguration;
    private final QUser users = new QUser("users");

    private final QBean<User> userBean = bean(User.class, users.id, users.login, users.name, users.password, users.email,
            users.authority, users.expiration_date, users.verification_id);

    @Autowired
    public SimpleUserRepository(DataSourceSwitcher dataSourceSwitcher, Configuration queryDslConfiguration) {
        this.dataSourceSwitcher = dataSourceSwitcher;
        this.queryDslConfiguration = queryDslConfiguration;
    }

    @Override
    public List<User> findByLogin(String login) {
        DataSource dataSource = dataSourceSwitcher.getCurrentDataSource();
        SQLQueryFactory queryFactory =
                new SQLQueryFactory(queryDslConfiguration, dataSource);
        return queryFactory.select(userBean).from(users).where(users.login.eq(login)).fetch();
    }

    @Override
    public List<User> findUserByName(String name) {
        return null;//TODO: implement this
    }

    @Override
    public List<User> findByEmail(String email) {
        return null;//TODO: implement this
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
        return null;//TODO: implement this
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
        return null;//TODO: implement this
    }

    @Override
    public Page<User> findAll(Predicate predicate, Pageable pageable) {
        return null;//TODO: implement this
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
        return null; //TODO: implement this
        //throw new UnsupportedOperationException("Not implemented yet!");
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
        return null;//TODO: implement this
    }

    @Override
    public Iterable<User> findAllById(Iterable<Long> ids) {
        return null;//TODO: implement this
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
}

package io.github.vssavin.umlib.repository;

import io.github.vssavin.umlib.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author vssavin on 18.12.2021
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, QuerydslPredicateExecutor<User> {
    List<User> findByLogin(String login);
    List<User> findUserByName(String name);
    List<User> findByEmail(String email);
    @Transactional
    void deleteByLogin(String login);
}

package com.github.vssavin.umlib.domain.security.csrf;

import com.github.vssavin.umlib.config.aspect.UmRouteDatasource;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Main repository of csrf.
 *
 * @author vssavin on 30.10.2023
 */
public interface UserCsrfTokenRepository extends CrudRepository<UserCsrfToken, Long> {

    @UmRouteDatasource
    List<UserCsrfToken> findByUserId(Long userId);

    @UmRouteDatasource
    @Transactional
    void deleteByToken(String token);

    @UmRouteDatasource
    @Transactional
    void deleteByUserId(Long userId);

}

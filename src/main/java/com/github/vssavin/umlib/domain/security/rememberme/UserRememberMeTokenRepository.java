package com.github.vssavin.umlib.domain.security.rememberme;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Main repository of remember-me.
 *
 * @author vssavin on 30.10.2023
 */
@Repository
public interface UserRememberMeTokenRepository extends CrudRepository<UserRememberMeToken, Long> {

    List<UserRememberMeToken> findByUserId(Long userId);

}

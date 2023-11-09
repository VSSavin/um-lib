package com.github.vssavin.umlib.domain.security.rememberme;

import com.github.vssavin.umlib.domain.user.User;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;

/**
 * @author vssavin on 08.11.2023
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractRememberMeServiceTest {

    protected final String rememberMeCookieName = "remember-me";

    protected final long adminUserId = 1;

    protected final User adminUser = new User("admin", "admin", "admin", "admin@example.com", "ROLE_ADMIN");

    protected AbstractRememberMeServices rememberMeServices;

    @Mock
    protected UserDetailsService userDetailsService;

    @Mock
    protected UserRememberMeTokenRepository userRememberMeTokenRepository;

    protected abstract void init();

    @Before
    public void setUp() {
        adminUser.setId(adminUserId);
        init();
    }

}

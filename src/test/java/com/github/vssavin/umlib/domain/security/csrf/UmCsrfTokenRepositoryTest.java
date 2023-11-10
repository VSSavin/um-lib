package com.github.vssavin.umlib.domain.security.csrf;

import com.github.vssavin.umlib.domain.security.rememberme.Authenticator;
import com.github.vssavin.umlib.domain.security.rememberme.RefreshOnAutologinTokenBasedRememberMeServices;
import com.github.vssavin.umlib.domain.security.rememberme.UserRememberMeTokenRepository;
import com.github.vssavin.umlib.domain.user.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;

import javax.servlet.http.Cookie;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * @author vssavin on 09.11.2023
 */
@RunWith(MockitoJUnitRunner.class)
public class UmCsrfTokenRepositoryTest {

    private final User adminUser = new User("admin", "admin", "admin", "admin@example.com", "ROLE_ADMIN");

    private final long adminUserId = 1;

    private final String rememberMeCookieName = "remember-me";

    private AbstractRememberMeServices rememberMeServices;

    private UmCsrfTokenRepository csrfTokenRepository;

    private Authenticator authenticator;

    private CsrfAuthenticationStrategy authenticationStrategy;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserRememberMeTokenRepository rememberMeTokenRepository;

    @Mock
    private UserCsrfTokenRepository userCsrfTokenRepository;

    @Before
    public void setUp() {
        adminUser.setId(adminUserId);
        rememberMeServices = new RefreshOnAutologinTokenBasedRememberMeServices(UUID.randomUUID().toString(),
                userDetailsService);
        rememberMeServices.setCookieName(rememberMeCookieName);
        authenticator = (Authenticator) rememberMeServices;
        csrfTokenRepository = new UmCsrfTokenRepository(authenticator, userCsrfTokenRepository,
                rememberMeTokenRepository);
        authenticationStrategy = new CsrfAuthenticationStrategy(csrfTokenRepository);
        when(userDetailsService.loadUserByUsername(adminUser.getLogin())).thenReturn(adminUser);
    }

    @Test
    public void shouldSaveTokenToStorageWhenCacheIsNotUsed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(rememberMeServices.getParameter(), "true");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = new UsernamePasswordAuthenticationToken(adminUser, adminUser.getPassword());
        rememberMeServices.loginSuccess(request, response, authentication);

        Cookie rememberMeCookie = response.getCookie(rememberMeCookieName);
        request.setCookies(rememberMeCookie);
        authenticationStrategy.onAuthentication(authentication, request, response);

        verify(userCsrfTokenRepository, atLeastOnce()).saveAll(any());
    }

    @Test
    public void shouldNotSaveTokenToStorageWhenCacheIsUsed() {
        csrfTokenRepository.setUseCache(true);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(rememberMeServices.getParameter(), "true");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = new UsernamePasswordAuthenticationToken(adminUser, adminUser.getPassword());
        rememberMeServices.loginSuccess(request, response, authentication);

        Cookie rememberMeCookie = response.getCookie(rememberMeCookieName);
        request.setCookies(rememberMeCookie);
        authenticationStrategy.onAuthentication(authentication, request, response);

        verify(userCsrfTokenRepository, times(0)).saveAll(any());
    }

}

package com.github.vssavin.umlib.domain.sucurity.rememberme;

import com.github.vssavin.umlib.domain.security.rememberme.Authenticator;
import com.github.vssavin.umlib.domain.security.rememberme.RefreshOnLoginDatabaseTokenBasedRememberMeService;
import com.github.vssavin.umlib.domain.security.rememberme.UserRememberMeTokenRepository;
import com.github.vssavin.umlib.domain.user.User;
import org.junit.Assert;
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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author vssavin on 07.11.2023
 */
@RunWith(MockitoJUnitRunner.class)
public class RefreshOnLoginDatabaseTokenBasedRememberMeServiceTest {

    private final String rememberMeCookieName = "remember-me";

    private final long adminUserId = 1;

    private final User adminUser = new User("admin", "admin", "admin", "admin@example.com", "ROLE_ADMIN");

    private final String wrongRememberMeCookieValue = "abcd";

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserRememberMeTokenRepository userRememberMeTokenRepository;

    private RefreshOnLoginDatabaseTokenBasedRememberMeService rememberMeServices;

    private Authenticator authenticator;

    @Before
    public void setUp() {
        adminUser.setId(adminUserId);
        rememberMeServices = new RefreshOnLoginDatabaseTokenBasedRememberMeService(UUID.randomUUID().toString(),
                userDetailsService, userRememberMeTokenRepository);
        authenticator = rememberMeServices;
        rememberMeServices.setCookieName(rememberMeCookieName);

        when(userDetailsService.loadUserByUsername(adminUser.getLogin())).thenReturn(adminUser);
    }

    @Test
    public void shouldAuthenticatorReturnNullWhenNoCookieDetected() {
        HttpServletRequest request = new MockHttpServletRequest();
        Authentication authentication = authenticator.retrieveAuthentication(request, new MockHttpServletResponse());
        Assert.assertNull(authentication);
    }

    @Test
    public void shouldAuthenticatorReturnNullWhenWrongCookieDetected() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Cookie rememberMeCookie = new Cookie(rememberMeCookieName, wrongRememberMeCookieValue);
        request.setCookies(rememberMeCookie);

        MockHttpServletResponse response = new MockHttpServletResponse();

        Authentication authentication = authenticator.retrieveAuthentication(request, response);
        Assert.assertNull(authentication);
    }

    @Test
    public void shouldAuthenticatorReturnAuthenticationWhenCookieOk() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = new UsernamePasswordAuthenticationToken(adminUser, adminUser.getPassword());
        rememberMeServices.onLoginSuccess(request, response, authentication);

        verify(userRememberMeTokenRepository, atLeastOnce()).save(any());

        Cookie rememberMeCookie = response.getCookie(rememberMeCookieName);

        request.setCookies(rememberMeCookie);

        authentication = authenticator.retrieveAuthentication(request, response);
        Assert.assertNotNull("Authentication shouldn't be null for requested user!", authentication);
        Assert.assertNotNull("Credentials shouldn't be null for requested user!", authentication.getCredentials());
        Assert.assertNotNull("Principal shouldn't be null for requested user!", authentication.getPrincipal());
    }

}

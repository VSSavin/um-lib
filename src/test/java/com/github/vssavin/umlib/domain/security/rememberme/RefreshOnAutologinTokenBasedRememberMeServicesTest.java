package com.github.vssavin.umlib.domain.security.rememberme;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * @author vssavin on 08.11.2023
 */

public class RefreshOnAutologinTokenBasedRememberMeServicesTest extends AbstractRememberMeServiceTest {

    private final String wrongRememberMeCookieValue = "abcd";

    private Authenticator authenticator;

    @Override
    protected void init() {
        rememberMeServices = new RefreshOnAutologinTokenBasedRememberMeServices(UUID.randomUUID().toString(),
                userDetailsService);
        authenticator = (Authenticator) rememberMeServices;
        rememberMeServices.setCookieName(rememberMeCookieName);

        when(userDetailsService.loadUserByUsername(adminUser.getLogin())).thenReturn(adminUser);
    }

    @Test
    public void shouldReturnNullWhenNoCookieDetected() {
        HttpServletRequest request = new MockHttpServletRequest();
        Authentication authentication = authenticator.retrieveAuthentication(request, new MockHttpServletResponse());
        Assert.assertNull(authentication);
    }

    @Test
    public void shouldReturnNullWhenWrongCookieDetected() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Cookie rememberMeCookie = new Cookie(rememberMeCookieName, wrongRememberMeCookieValue);
        request.setCookies(rememberMeCookie);

        MockHttpServletResponse response = new MockHttpServletResponse();

        Authentication authentication = authenticator.retrieveAuthentication(request, response);
        Assert.assertNull(authentication);
    }

    @Test
    public void shouldReturnAuthenticationWhenCookieOk() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(rememberMeServices.getParameter(), "true");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = new UsernamePasswordAuthenticationToken(adminUser, adminUser.getPassword());
        rememberMeServices.loginSuccess(request, response, authentication);

        Cookie rememberMeCookie = response.getCookie(rememberMeCookieName);

        request.setCookies(rememberMeCookie);

        authentication = authenticator.retrieveAuthentication(request, response);
        Assert.assertNotNull("Authentication shouldn't be null for requested user!", authentication);
        Assert.assertNotNull("Credentials shouldn't be null for requested user!", authentication.getCredentials());
        Assert.assertNotNull("Principal shouldn't be null for requested user!", authentication.getPrincipal());
    }

    @Test
    public void shouldResponseContainsCookieWhenAutoLoginOk() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(rememberMeServices.getParameter(), "true");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = new UsernamePasswordAuthenticationToken(adminUser, adminUser.getPassword());
        rememberMeServices.loginSuccess(request, response, authentication);

        Cookie loginRememberMeCookie = response.getCookie(rememberMeCookieName);

        Assert.assertNotNull("Remember-me cookie shouldn't be null!", loginRememberMeCookie);

        request.setCookies(loginRememberMeCookie);

        response = new MockHttpServletResponse();

        rememberMeServices.autoLogin(request, response);

        Cookie autoLoginRememberMeCookie = response.getCookie(rememberMeCookieName);

        Assert.assertNotNull("Remember-me autoLogin cookie shouldn't be null!", autoLoginRememberMeCookie);
    }

}

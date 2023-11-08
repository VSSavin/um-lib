package com.github.vssavin.umlib.domain.sucurity.rememberme;

import com.github.vssavin.umlib.domain.security.rememberme.RefreshOnLoginDatabaseTokenBasedRememberMeService;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.servlet.http.Cookie;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author vssavin on 07.11.2023
 */
public class RefreshOnLoginDatabaseTokenBasedRememberMeServiceTest extends AbstractRememberMeServiceTest {

    @Override
    protected void init() {
        rememberMeServices = new RefreshOnLoginDatabaseTokenBasedRememberMeService(UUID.randomUUID().toString(),
                userDetailsService, userRememberMeTokenRepository);
        rememberMeServices.setCookieName(rememberMeCookieName);

        when(userDetailsService.loadUserByUsername(adminUser.getLogin())).thenReturn(adminUser);
    }

    @Test
    public void shouldTokenRepositorySaveTokenWhenLoginSuccess() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(rememberMeServices.getParameter(), "true");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = new UsernamePasswordAuthenticationToken(adminUser, adminUser.getPassword());

        rememberMeServices.loginSuccess(request, response, authentication);

        verify(userRememberMeTokenRepository, atLeastOnce()).save(any());

    }

    @Test
    public void shouldTokenRepositorySaveTokenWhenAutoLoginSuccess() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(rememberMeServices.getParameter(), "true");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = new UsernamePasswordAuthenticationToken(adminUser, adminUser.getPassword());

        rememberMeServices.loginSuccess(request, response, authentication);
        Cookie rememberMeCookie = response.getCookie(rememberMeCookieName);
        request.setCookies(rememberMeCookie);

        rememberMeServices.autoLogin(request, response);

        verify(userRememberMeTokenRepository, atLeast(2)).save(any());
    }

}

package com.github.vssavin.umlib.domain.auth;

import com.github.vssavin.umlib.AbstractTest;
import com.github.vssavin.umlib.config.UmConfig;
import com.github.vssavin.umlib.domain.event.EventType;
import com.github.vssavin.umlib.domain.user.User;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * @author vssavin on 01.09.2023
 */
public class SimpleAuthServiceTest extends AbstractTest {
    private static final Logger log = LoggerFactory.getLogger(SimpleAuthServiceTest.class);

    private final User incorrectAdminUser = new User("admin", "admin", "notadmin",
            "admin@example.com", "ADMIN");

    private final User correctAdminUser = testAdminUser;

    private final HttpServletRequest request = new MockHttpServletRequest();
    private final HttpServletResponse response = new MockHttpServletResponse();

    private AuthService simpleAuthService;

    private int maxAuthFailureCount;

    @Autowired
    public void setSimpleAuthService(AuthService simpleAuthService) {
        this.simpleAuthService = simpleAuthService;
    }

    @Autowired
    @Override
    public void setUmConfig(UmConfig umConfig) {
        super.setUmConfig(umConfig);
        this.maxAuthFailureCount = umConfig.getMaxAuthFailureCount();
    }

    @Test(expected = BadCredentialsException.class)
    public void shouldThrowExceptionForIncorrectCredentials() throws Exception {
        Authentication authentication = getUserAuthentication(incorrectAdminUser, request);
        simpleAuthService.authenticate(authentication);
    }

    @Test
    public void shouldReturnAuthenticationForCorrectCredentials() throws Exception {
        log.info("Using user password: " + correctAdminUser.getPassword());
        Authentication authentication = getUserAuthentication(correctAdminUser, request);
        Authentication returnedAuthentication = simpleAuthService.authenticate(authentication);
        Assertions.assertNotNull(returnedAuthentication);
        Assertions.assertEquals(returnedAuthentication.getCredentials(), correctAdminUser.getPassword());
    }

    @Test
    public void shouldAuthoritiesNotEmptyWhenLoggedIn() throws Exception {
        Authentication authentication = getUserAuthentication(correctAdminUser, request);
        Collection<GrantedAuthority> authorities =
                simpleAuthService.processSuccessAuthentication(authentication, request, EventType.LOGGED_IN);
        Assertions.assertNotNull(authorities);
        Assertions.assertFalse(authorities.isEmpty());
    }

    @Test(expected = AuthenticationForbiddenException.class)
    public void shouldThrowExceptionForTooManyFailedAuthentication() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        HttpServletResponse mockResponse = new MockHttpServletResponse();
        mockRequest.setRemoteAddr("10.5.0.1");
        for (int i = 0; i < maxAuthFailureCount; i++) {
            simpleAuthService.processFailureAuthentication(mockRequest, mockResponse, new BadCredentialsException(""));
        }
    }

    @Test
    public void shouldAuthenticationNotAllowedForTooManyFailedAuthentication() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        HttpServletResponse mockResponse = new MockHttpServletResponse();
        mockRequest.setRemoteAddr("10.5.0.1");
        for (int i = 0; i < maxAuthFailureCount; i++) {
            try {
                simpleAuthService.processFailureAuthentication(
                        mockRequest, mockResponse, new BadCredentialsException(""));
            } catch (AuthenticationForbiddenException e) {
                //ignore
                break;
            }
        }

        Assertions.assertFalse(simpleAuthService.isAuthenticationAllowed(mockRequest.getRemoteAddr()));
    }

    private Authentication getUserAuthentication(User user, HttpServletRequest request) throws Exception {
        String principal = user.getLogin();
        String encryptedCredentials = encrypt("", user.getPassword());
        Object authenticationDetails = new WebAuthenticationDetails(request.getRemoteAddr(), "");
        UsernamePasswordAuthenticationToken authentication =
                new CustomUsernamePasswordAuthenticationToken(principal, encryptedCredentials);
        authentication.setDetails(authenticationDetails);
        return authentication;
    }

}

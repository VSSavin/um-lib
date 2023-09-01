package com.github.vssavin.umlib.domain.security.spring;

import com.github.vssavin.umlib.domain.auth.AuthService;
import com.github.vssavin.umlib.domain.auth.AuthenticationForbiddenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * An {@link org.springframework.security.web.authentication.AuthenticationFailureHandler} implementation
 * that handles failed authentication using corresponding authentication service.
 *
 * @author vssavin on 18.12.2021
 */
@Component
class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private static final String FAILURE_REDIRECT_PAGE = "/login.html?error=true";

    private final AuthService authService;

    @Autowired
    CustomAuthenticationFailureHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException {

        String lang = request.getParameter("lang");
        if (lang != null) {
            lang = "&lang=" + lang;
        } else {
            lang = "";
        }

        try {
            authService.processFailureAuthentication(request, response, exception);
        } catch (AuthenticationForbiddenException e) {
            response.sendError(HttpStatus.FORBIDDEN.value(), e.getMessage());
        }

        response.sendRedirect(FAILURE_REDIRECT_PAGE + lang);

    }
}

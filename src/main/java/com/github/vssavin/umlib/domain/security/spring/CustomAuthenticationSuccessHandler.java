package com.github.vssavin.umlib.domain.security.spring;

import com.github.vssavin.umlib.config.UmConfig;
import com.github.vssavin.umlib.domain.auth.AuthService;
import com.github.vssavin.umlib.domain.event.EventType;
import com.github.vssavin.umlib.domain.user.UserExpiredException;
import com.github.vssavin.umlib.domain.user.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * An {@link org.springframework.security.web.authentication.AuthenticationSuccessHandler}
 * implementation that attempts to authenticate using corresponding authentication
 * service.
 *
 * @author vssavin on 22.12.21
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    private final UmConfig umConfig;

    public CustomAuthenticationSuccessHandler(AuthService authService, UmConfig umConfig) {
        this.authService = authService;
        this.umConfig = umConfig;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        String successUrl = umConfig.getSuccessUrl();

        Collection<GrantedAuthority> authorities = Collections.emptyList();
        try {
            authorities = authService.processSuccessAuthentication(authentication, request, EventType.LOGGED_IN);
        }
        catch (UserExpiredException e) {
            successUrl = UmConfig.LOGIN_URL + "?error=true";
        }

        if (authorities.stream().anyMatch(authority -> authority.getAuthority().equals(Role.ROLE_ADMIN.name()))) {
            successUrl = umConfig.getAdminSuccessUrl();
        }

        String lang = request.getParameter("lang");
        String delimiter = "?";
        if (successUrl.contains("?")) {
            delimiter = "&";
        }
        if (lang != null) {
            lang = delimiter + "lang=" + lang;
        }
        else {
            lang = "";
        }

        response.sendRedirect(successUrl + lang);
    }

}

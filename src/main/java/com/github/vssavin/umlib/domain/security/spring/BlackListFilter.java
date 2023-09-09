package com.github.vssavin.umlib.domain.security.spring;

import com.github.vssavin.umlib.domain.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Security filter to check if user ip was blocked.
 *
 * @author vssavin on 18.12.2021
 */
@Component
public class BlackListFilter extends GenericFilterBean {
    private final AuthService authService;

    @Autowired
    public BlackListFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String userIp = request.getRemoteAddr();
        if (!authService.isAuthenticationAllowed(userIp)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendError(HttpStatus.FORBIDDEN.value(), "Access denied!");
            return;
        }

        chain.doFilter(request, response);
    }
}

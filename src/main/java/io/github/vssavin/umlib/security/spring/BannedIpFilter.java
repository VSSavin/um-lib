package io.github.vssavin.umlib.security.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author vssavin on 18.12.2021
 */
@Component
public class BannedIpFilter extends GenericFilterBean {
    private static final Logger log = LoggerFactory.getLogger(BannedIpFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String userIp = request.getRemoteAddr();
        if (CustomAuthenticationFailureHandler.isBannedIp(userIp)) {
            log.info("Trying to access from banned IP: " + userIp);
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendError(HttpStatus.FORBIDDEN.value(), "Доступ запрещен");
            return;
        }
        chain.doFilter(request, response);
    }
}

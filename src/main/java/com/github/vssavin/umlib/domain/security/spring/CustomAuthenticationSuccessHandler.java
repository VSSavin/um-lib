package com.github.vssavin.umlib.domain.security.spring;

import com.github.vssavin.umlib.config.DataSourceSwitcher;
import com.github.vssavin.umlib.config.UmConfig;
import com.github.vssavin.umlib.domain.event.EventType;
import com.github.vssavin.umlib.domain.user.UserExpiredException;
import com.github.vssavin.umlib.domain.user.UserService;
import com.github.vssavin.umlib.domain.user.Role;
import com.github.vssavin.umlib.domain.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * An {@link org.springframework.security.web.authentication.AuthenticationSuccessHandler} implementation
 * that attempts to authenticate the user using o2Auth or user/password mechanism.
 *
 * @author vssavin on 22.12.21
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final DataSourceSwitcher dataSourceSwitcher;
    private final UserService userService;
    private final UmConfig umConfig;

    public CustomAuthenticationSuccessHandler(DataSourceSwitcher dataSourceSwitcher, UserService userService,
                                              UmConfig umConfig) {
        this.dataSourceSwitcher = dataSourceSwitcher;
        this.userService = userService;
        this.umConfig = umConfig;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String successUrl = umConfig.getSuccessUrl();

        User user = null;
        try {
            dataSourceSwitcher.switchToUmDataSource();
            user = userService.processSuccessAuthentication(authentication, request, EventType.LOGGED_IN);
            dataSourceSwitcher.switchToPreviousDataSource();
        } catch (UserExpiredException e) {
            successUrl = UmConfig.LOGIN_URL + "?error=true";
        }

        if (user != null && (user.getAuthority().equals(Role.ROLE_ADMIN.name()))) {
                successUrl = umConfig.getAdminSuccessUrl();

        }

        String lang = request.getParameter("lang");
        String delimiter = "?";
        if (successUrl.contains("?")) {
            delimiter = "&";
        }
        if (lang != null) {
            lang = delimiter + "lang=" + lang;
        } else {
            lang = "";
        }

        response.sendRedirect(successUrl + lang);
    }
}

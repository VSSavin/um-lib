package io.github.vssavin.umlib.security.spring;

import io.github.vssavin.umlib.config.UmConfig;
import io.github.vssavin.umlib.entity.Role;
import io.github.vssavin.umlib.entity.User;
import io.github.vssavin.umlib.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * @author vssavin on 22.12.21
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    public CustomAuthenticationSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String successUrl = UmConfig.successUrl;

        User user = userService.getUserByLogin(authentication.getPrincipal().toString());
        if (user != null) {
            if (user.getAuthority().equals(Role.ROLE_ADMIN.name())) successUrl = UmConfig.adminSuccessUrl;
            if (user.getExpirationDate().before(new Date())) {
                userService.deleteUser(user);
                successUrl = UmConfig.LOGIN_URL + "?error=true";
            }
        }

        String lang = request.getParameter("lang");
        String delimiter = "?";
        if (successUrl.contains("?")) delimiter = "&";
        if (lang != null) lang = delimiter + "lang=" + lang;
        else lang = "";
        response.sendRedirect(successUrl + lang);
    }
}

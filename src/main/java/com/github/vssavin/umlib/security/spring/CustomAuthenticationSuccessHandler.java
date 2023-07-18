package com.github.vssavin.umlib.security.spring;

import com.github.vssavin.umlib.config.UmConfig;
import com.github.vssavin.umlib.user.UserService;
import com.github.vssavin.umlib.user.Role;
import com.github.vssavin.umlib.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
    private final UmConfig umConfig;

    public CustomAuthenticationSuccessHandler(UserService userService, UmConfig umConfig) {
        this.userService = userService;
        this.umConfig = umConfig;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String successUrl = umConfig.getSuccessUrl();
        User user = null;
        try {
            OAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            user = userService.processOAuthPostLogin(oAuth2User);
            if (user.getAuthority().equals(Role.ROLE_ADMIN.name())) {
                successUrl = umConfig.getAdminSuccessUrl();
            }
        } catch (ClassCastException e) {
            //ignore, it's ok
        }

        if (user == null) {
            user = userService.getUserByLogin(authentication.getPrincipal().toString());
            if (user != null) {
                if (user.getAuthority().equals(Role.ROLE_ADMIN.name())) {
                    successUrl = umConfig.getAdminSuccessUrl();
                }
                if (user.getExpirationDate().before(new Date())) {
                    userService.deleteUser(user);
                    successUrl = UmConfig.LOGIN_URL + "?error=true";
                }
            }
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

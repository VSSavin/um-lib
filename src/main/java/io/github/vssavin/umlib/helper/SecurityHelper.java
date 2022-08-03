package io.github.vssavin.umlib.helper;

import io.github.vssavin.umlib.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Created by vssavin on 02.08.2022.
 */
public class SecurityHelper {

    private SecurityHelper() {}

    public static String getAuthorizedUserName(UserService userService) {
        String authorizedUserName = getAuthorizedUserLogin();
        if (!authorizedUserName.isEmpty() && userService != null) {
            try {
                authorizedUserName = userService.getUserByName(authorizedUserName).getName();
            } catch (UsernameNotFoundException e) {
                authorizedUserName = "";
            }
        }
        return authorizedUserName;
    }

    public static String getAuthorizedUserLogin() {
        String authorizedUserName = "";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            authorizedUserName = authentication.getName();
        }
        return authorizedUserName;
    }

}

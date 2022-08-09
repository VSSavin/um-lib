package io.github.vssavin.umlib.helper;

import io.github.vssavin.umlib.entity.Role;
import io.github.vssavin.umlib.entity.User;
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
                authorizedUserName = userService.getUserByLogin(authorizedUserName).getName();
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

    public static boolean isAuthorizedAdmin(UserService userService) {
        String login = getAuthorizedUserLogin();
        boolean isAdminUser = false;
        if (!login.isEmpty() && userService != null) {
            try {
                User user = userService.getUserByLogin(login);
                String authority = user.getAuthority();
                Role role = Role.getRole(authority);
                if (role.equals(Role.ROLE_ADMIN)) isAdminUser = true;
            } catch (UsernameNotFoundException ignored) {
            }
        }
        return isAdminUser;
    }

}

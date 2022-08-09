package io.github.vssavin.umlib.utils;

import io.github.vssavin.umlib.entity.User;
import io.github.vssavin.umlib.service.UserService;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 * @author vssavin on 09.08.2022
 */
public class UserUtils {

    private UserUtils(){}

    public static Long getUserId(HttpServletRequest request, UserService userService) {
        Principal principal = request.getUserPrincipal();
        long userId = -1L;
        if (principal != null) {
            User user = userService.getUserByLogin(principal.getName());
            userId = user.getId();
        }
        return userId;
    }

    public static void addUsernameToModel(HttpServletRequest request, ModelAndView modelAndView) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            modelAndView.addObject("username", principal.getName());
        }
    }
}

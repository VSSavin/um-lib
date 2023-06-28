package io.github.vssavin.umlib.user;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import io.github.vssavin.umlib.entity.QUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

import static com.querydsl.core.types.Projections.bean;

/**
 * @author vssavin on 09.08.2022
 */
public class UserUtils {
    private static final Logger log = LoggerFactory.getLogger(UserUtils.class);

    private static final QUser users = new QUser("users");
    private static final QBean<User> userBean = bean(User.class, users.id, users.login, users.name,
            users.password, users.email, users.authority, users.expiration_date, users.verification_id);

    private UserUtils(){}

    public static Long getUserId(HttpServletRequest request, UserService userService) {
        long userId;
        userId = getAuthorizedUser(request, userService).getId();
        return userId;
    }

    public static void addUsernameToModel(HttpServletRequest request, ModelAndView modelAndView) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            modelAndView.addObject("login", principal.getName());
        }
    }

    public static void addUsernameToModel(HttpServletRequest request, UserService userService,
                                          ModelAndView modelAndView) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            try {
                User user = getAuthorizedUser(request, userService);
                if (user != null) modelAndView.addObject("username", user.getName());
            } catch (Exception e) {
                log.error("Adding username to model failed!", e);
                throw new RuntimeException(e.getMessage(), e.getCause());
            }

        }
    }

    public static boolean isAuthorizedAdmin(HttpServletRequest request, UserService userService) {
        User user = getAuthorizedUser(request, userService);
        return user != null && Role.getRole(user.getAuthority()) == Role.ROLE_ADMIN;
    }

    public static boolean isAuthorizedUser(HttpServletRequest request, UserService userService) {
        User user = getAuthorizedUser(request, userService);
        return user != null && Role.getRole(user.getAuthority()) == Role.ROLE_USER;
    }

    public static User getAuthorizedUser(HttpServletRequest request, UserService userService) {
        Principal principal = request.getUserPrincipal();
        User user = null;
        if (principal != null) {
            try {
                OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) principal;
                user = userService.getUserByOAuth2Token(token);
            } catch (ClassCastException e) {
                //ignore, it's ok'
            }
            if (user == null) {
                user = userService.getUserByLogin(principal.getName());
            }

        }
        return user;
    }

    public static Predicate userFilterToPredicate(UserFilter userFilter) {
        BooleanExpression expression = null;
        expression = processAndEqualLong(expression, users.id, userFilter.getUserId());
        expression = processAndLikeString(expression, users.email, userFilter.getEmail());
        expression = processAndLikeString(expression, users.name, userFilter.getName());
        expression = processAndLikeString(expression, users.login, userFilter.getLogin());
        return expression;
    }

    private static BooleanExpression processAndEqualLong(BooleanExpression expression,
                                                         SimpleExpression<Long> simpleExpression, Long value) {
        if (value != null) {
            if (expression != null) {
                expression = expression.and(simpleExpression.eq(value));
            } else {
                expression = simpleExpression.eq(value);
            }
        }

        return expression;
    }

    private static BooleanExpression processAndLikeString(BooleanExpression expression,
                                                          StringExpression stringExpression, String value) {
        if (value != null && !value.isEmpty()) {
            if (expression != null) {
                expression = expression.and(stringExpression.like(value));
            } else {
                expression = stringExpression.like(value);
            }
        }

        return expression;
    }
}

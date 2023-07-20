package com.github.vssavin.umlib.user;

import com.github.vssavin.umlib.config.DataSourceSwitcher;
import com.github.vssavin.umlib.data.pagination.Paged;
import com.github.vssavin.umlib.email.EmailNotFoundException;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Nonnull;
import java.util.Collections;

/**
 * @author vssavin on 20.07.2023
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private DataSourceSwitcher dataSourceSwitcher;

    @InjectMocks
    private UserServiceImpl userService;

    private final User emptyUser = new User("", "", "", "", "");
    private final User adminUser =
            new User("admin", "admin", "", "admin@example.com", "ROLE_ADMIN");
    private final Long id = 1L;
    private final UserFilter adminFilter = new UserFilter(null, "admin", "", "");
    private final UserFilter wrongIdFilter = new UserFilter(-1L, "", "", "");
    private final Pageable pageOneSizeOne = PageRequest.of(0, 1);


    @Before
    public void setUp() {
        Mockito.when(userRepository.findUserByName(adminUser.getName())).thenReturn(Collections.singletonList(adminUser));
        Mockito.when(userRepository.findByLogin(adminUser.getLogin())).thenReturn(Collections.singletonList(adminUser));
        Mockito.when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Collections.singletonList(adminUser));
        Mockito.when(userRepository.findById(null)).thenThrow(IllegalArgumentException.class);
        Mockito.when(userRepository.findAll(pageOneSizeOne))
                .thenReturn(new PageImpl<>(Collections.singletonList(adminUser), pageOneSizeOne, 1));
        Mockito.when(userRepository.findAll(userFilterToPredicate(adminFilter), pageOneSizeOne))
                .thenReturn(new PageImpl<>(Collections.singletonList(adminUser), pageOneSizeOne, 1));
        Mockito.when(userRepository.findAll(userFilterToPredicate(wrongIdFilter), pageOneSizeOne))
                .thenReturn(Page.empty());
    }

    @Test(expected = UserServiceException.class)
    public void shouldThrownExceptionWhenGetUsersFilterIsNull() {
        userService.getUsers(null, 0, 0);
    }

    @Test(expected = UserServiceException.class)
    public void shouldThrownExceptionWhenGetUsersWrongPageNumber0() {
        userService.getUsers(new UserFilter(1L, "", "", ""), 0, 0);
    }

    @Test(expected = UserServiceException.class)
    public void shouldThrownExceptionWhenGetUsersWrongPageNumber1() {
        userService.getUsers(new UserFilter(1L, "", "", ""), 1, 0);
    }

    @Test(expected = UserServiceException.class)
    public void shouldThrownExceptionWhenGetUsersWrongPageSize() {
        userService.getUsers(new UserFilter(1L, "", "", ""), 3, 0);
    }

    @Test
    public void shouldGetUsersEmptyPage() {
        UserFilter filter = new UserFilter(-1L, "", "", "");
        Paged<User> users = userService.getUsers(filter, 1, 1);
        Assert.assertTrue(users.getPage().getContent().isEmpty());
    }

    @Test
    public void shouldGetUsersNotEmptyPageWhenParamsValid() {
        Paged<User> users = userService.getUsers(null, 1, 1);
        Assert.assertFalse(users.getPage().getContent().isEmpty());
    }

    @Test
    public void shouldGetUsersNotEmptyPage() {
        UserFilter filter = new UserFilter(null, "admin", "", "");
        Paged<User> users = userService.getUsers(filter, 1, 1);
        Assert.assertFalse(users.getPage().getContent().isEmpty());
    }

    @Test(expected = UserServiceException.class)
    public void shouldThrownExceptionWhenGetUserByIdNullId() {
        userService.getUserById(null);
    }

    @Test(expected = UserNotFoundException.class)
    public void shouldThrownExceptionWhenGetUserByIdWrongId() {
        userService.getUserById(-1L);
    }

    @Test(expected = UsernameNotFoundException.class)
    public void shouldThrownExceptionWhenGetUserByNameNonExistentName() {
        userService.getUserByName("");
    }

    @Test
    public void shouldGetUserByNameExistentName() {
        User user = userService.getUserByName("admin");
        Assert.assertEquals("admin", user.getName());
    }

    @Test(expected = UsernameNotFoundException.class)
    public void shouldThrownExceptionWhenGetUserByLoginNonExistentLogin() {
        userService.getUserByName("");
    }

    @Test
    public void shouldGetUserByLoginExistentLogin() {
        User user = userService.getUserByLogin("admin");
        Assert.assertEquals("admin", user.getLogin());
    }

    @Test(expected = EmailNotFoundException.class)
    public void shouldThrownExceptionWhenGetUserByEmailNonExistentEmail() {
        userService.getUserByEmail("non-existent-email");
    }

    @Test
    public void shouldGetUserByEmailExistentEmail() {
        User user = userService.getUserByEmail("admin@example.com");
        Assert.assertEquals("admin@example.com", user.getEmail());
    }

    @Nonnull
    private Predicate userFilterToPredicate(UserFilter userFilter) {
        BooleanExpression expression = null;
        QUser user = QUser.user;
        expression = processAndEqualLong(expression, user.id, userFilter.getUserId());
        expression = processAndLikeString(expression, user.email, userFilter.getEmail());
        expression = processAndLikeString(expression, user.name, userFilter.getName());
        expression = processAndLikeString(expression, user.login, userFilter.getLogin());
        return expression;
    }

    @Nonnull
    private BooleanExpression processAndEqualLong(BooleanExpression expression,
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

    @Nonnull
    private BooleanExpression processAndLikeString(BooleanExpression expression,
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

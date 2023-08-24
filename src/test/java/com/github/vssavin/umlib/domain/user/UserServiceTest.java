package com.github.vssavin.umlib.domain.user;

import com.github.vssavin.umlib.config.DataSourceSwitcher;
import com.github.vssavin.umlib.data.pagination.Paged;
import com.github.vssavin.umlib.domain.email.EmailNotFoundException;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import javax.annotation.Nonnull;
import java.util.*;

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
    private final User oAuthUser =
            new User("newUser@gmail.com", "newUser", "", "newUser@gmail.com", "");
    private final User adminUser =
            new User("admin", "admin", "", "admin@example.com", "ROLE_ADMIN");
    private final UserFilter adminFilter = new UserFilter(null, "admin", "", "");
    private final UserFilter wrongIdFilter = new UserFilter(-1L, "", "", "");
    private final Pageable pageOneSizeOne = PageRequest.of(0, 1);


    @Before
    public void setUp() {
        Mockito.when(userRepository.findUserByName(adminUser.getName())).thenReturn(Collections.singletonList(adminUser));
        Mockito.when(userRepository.findByLogin(adminUser.getLogin())).thenReturn(Collections.singletonList(adminUser));
        Mockito.when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Collections.singletonList(adminUser));

        Mockito.when(userRepository.findByLogin(oAuthUser.getLogin()))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.singletonList(oAuthUser));

        Mockito.when(userRepository.findByEmail(oAuthUser.getLogin()))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.singletonList(oAuthUser));

        Mockito.when(userRepository.save(oAuthUser)).thenReturn(oAuthUser);
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
        UserFilter filter = new UserFilter(null, adminUser.getLogin(), "", "");
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
        User user = userService.getUserByName(adminUser.getName());
        Assert.assertEquals(adminUser.getName(), user.getName());
    }

    @Test(expected = UsernameNotFoundException.class)
    public void shouldThrownExceptionWhenGetUserByLoginNonExistentLogin() {
        userService.getUserByName("");
    }

    @Test
    public void shouldGetUserByLoginExistentLogin() {
        User user = userService.getUserByLogin(adminUser.getLogin());
        Assert.assertEquals(adminUser.getLogin(), user.getLogin());
    }

    @Test(expected = EmailNotFoundException.class)
    public void shouldThrownExceptionWhenGetUserByEmailNonExistentEmail() {
        userService.getUserByEmail("non-existent-email");
    }

    @Test
    public void shouldGetUserByEmailExistentEmail() {
        User user = userService.getUserByEmail(adminUser.getEmail());
        Assert.assertEquals(adminUser.getEmail(), user.getEmail());
    }

    @Test
    public void shouldGetUserRecoveryIdExistentLogin() {
        Map<String, User> recoveryIds = userService.getUserRecoveryId(adminUser.getLogin());
        Optional<String> optionalRecoveryId = recoveryIds.keySet().stream().findFirst();
        Assert.assertTrue(optionalRecoveryId.isPresent());
        User user = userService.getUserByRecoveryId(optionalRecoveryId.get());
        Assert.assertEquals(user, adminUser);
    }

    @Test
    public void shouldGetUserRecoveryIdExistentEmail() {
        Map<String, User> recoveryIds = userService.getUserRecoveryId(adminUser.getEmail());
        Optional<String> optionalRecoveryId = recoveryIds.keySet().stream().findFirst();
        Assert.assertTrue(optionalRecoveryId.isPresent());
        User user = userService.getUserByRecoveryId(optionalRecoveryId.get());
        Assert.assertEquals(user, adminUser);
    }

    @Test(expected = UserServiceException.class)
    public void shouldGetUserRecoveryIdNonExistentLogin() {
        userService.getUserRecoveryId(emptyUser.getLogin());
    }

    @Test(expected = UserServiceException.class)
    public void shouldGetUserRecoveryIdNotExistentRecoveryId() {
        userService.getUserByRecoveryId("");
    }

    @Test
    public void shouldProcessOauthPostLoginUserExists() {
        OAuth2User oAuth2User = createUser(adminUser.getEmail());
        User user = userService.processOAuthPostLogin(oAuth2User);
        Assert.assertEquals(adminUser, user);
    }

    @Test
    public void shouldProcessOauthPostLoginUserNotExists() {
        OAuth2User oAuth2User = createUser(oAuthUser.getEmail());
        User user = userService.processOAuthPostLogin(oAuth2User);
        Assert.assertEquals(oAuthUser, user);
    }

    @Test
    public void shouldGetUserByOAuth2TokenUserExists() {
        Mockito.when(userRepository.findByEmail(oAuthUser.getLogin())).thenReturn(Collections.singletonList(oAuthUser));
        OAuth2User oAuth2User = createUser(oAuthUser.getEmail());
        OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(oAuth2User, oAuth2User.getAuthorities(), "id");
        User user = userService.getUserByOAuth2Token(token);
        Assert.assertEquals(oAuthUser, user);
    }

    @Test
    public void shouldGetUserByOAuth2TokenUserNotExists() {
        OAuth2User oAuth2User = createUser(emptyUser.getEmail());
        OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(oAuth2User, oAuth2User.getAuthorities(), "id");
        User user = userService.getUserByOAuth2Token(token);
        Assert.assertNull(user);
    }

    private OAuth2User createUser(String email) {
        Map<String, Object> attributesMap = new HashMap<>();
        String nameAttributeKey = "email";
        attributesMap.put(nameAttributeKey, email);
        Collection<? extends GrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority(Role.getStringRole(Role.ROLE_USER)));
        return new DefaultOAuth2User(authorities, attributesMap, nameAttributeKey);
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

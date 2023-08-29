package com.github.vssavin.umlib.domain.user;

import com.github.vssavin.umlib.base.repository.*;
import com.github.vssavin.umlib.domain.event.EventDto;
import com.github.vssavin.umlib.domain.event.EventService;
import com.github.vssavin.umlib.domain.event.EventType;
import com.querydsl.core.types.Predicate;
import com.github.vssavin.umlib.config.DataSourceSwitcher;
import com.github.vssavin.umlib.domain.email.EmailNotFoundException;
import com.github.vssavin.umlib.data.pagination.Paged;
import com.github.vssavin.umlib.data.pagination.Paging;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Main implementation of user management service.
 *
 * @author vssavin on 18.12.2021
 */
@Service
public class UserServiceImpl implements UserService {
    private static final Map<String, UserRecoveryParams> passwordRecoveryIds = new ConcurrentHashMap<>();

    private final EventService eventService;
    private final PasswordEncoder passwordEncoder;
    private final UmRepositorySupport<UserRepository, User> repositorySupport;

    @Autowired
    public UserServiceImpl(EventService eventService, UserRepository userRepository, PasswordEncoder passwordEncoder,
                           DataSourceSwitcher dataSourceSwitcher) {
        this.eventService = eventService;
        this.passwordEncoder = passwordEncoder;
        this.repositorySupport = new UmRepositorySupport<>(userRepository, dataSourceSwitcher);
    }

    @Override
    public Paged<User> getUsers(UserFilter userFilter, int pageNumber, int size) {
        String message = "Error while search user with params: pageNumber = %d, size = %d, filter: [%s]!";
        Object[] params = {pageNumber, size, userFilter};
        Page<User> users;
        PagedRepositoryFunction<UserRepository, User> function;
        Pageable pageable;
        try {
            pageable = PageRequest.of(pageNumber - 1, size);
        } catch (Exception e) {
            throw new UserServiceException(String.format(message, params), e);
        }

        if (userFilter == null || userFilter.isEmpty()) {
            function = repository -> repository.findAll(pageable);
        } else {
            Predicate predicate = userFilterToPredicate(userFilter);
            function = repository -> repository.findAll(predicate, pageable);
        }

        users = repositorySupport.execute(function, message, params);

        return new Paged<>(users, Paging.of(users.getTotalPages(), pageNumber, size));
    }

    @Override
    public User getUserById(Long id) {
        String message = "Getting a user by id = %d error!";
        Object[] params = {id};
        RepositoryOptionalFunction<UserRepository, User> function = repository -> repository.findById(id);
        Optional<User> user = repositorySupport.execute(function, message, params);

        if (!user.isPresent()) {
            throw new UserNotFoundException(String.format("User with id = %d not found!", id));
        }

        return user.get();
    }

    @Override
    public User addUser(User user) {
        String message = "Adding error for user [%s]!";
        Object[] params = {user};
        RepositoryFunction<UserRepository, User> function = repository -> repository.save(user);
        return repositorySupport.execute(function, message, params);
    }

    @Override
    public User updateUser(User user) {
        String message = "Update error for user [%s]";
        Object[] params = {user};
        RepositoryFunction<UserRepository, User> function = repository -> repository.save(user);
        return repositorySupport.execute(function, message, params);
    }

    @Override
    public User getUserByName(String name) {
        String message = "Error while getting user by name [%s]";
        Object[] params = {name};
        RepositoryListFunction<UserRepository, User> function = repo -> repo.findUserByName(name);
        List<User> users = repositorySupport.execute(function, message, params);
        if (!users.isEmpty()) {
            return users.get(0);
        }
        throw new UsernameNotFoundException(String.format("User: %s not found!", name));
    }

    @Override
    public User getUserByLogin(String login) {
        String message = "Error while getting user by login [%s]";
        Object[] params = {login};
        RepositoryListFunction<UserRepository, User> function = repo -> repo.findByLogin(login);
        List<User> users = repositorySupport.execute(function, message, params);
        if (!users.isEmpty()) {
            return users.get(0);
        }
        throw new UsernameNotFoundException(String.format("User with login: %s not found!", login));

    }

    @Override
    public User getUserByEmail(String email) {
        String message = "Error while getting user by email [%s]";
        Object[] params = {email};
        RepositoryListFunction<UserRepository, User> function = repo -> repo.findByEmail(email);
        List<User> users = repositorySupport.execute(function, message, params);
        if (!users.isEmpty()) {
            return users.get(0);
        }
        throw new EmailNotFoundException(String.format("Email: %s not found!", email));
    }

    @Override
    public void deleteUser(User user) {
        Objects.requireNonNull(user, "User must not be null!");
        String message = "Error while deleting user [%s]";
        Object[] params = {user};
        RepositoryConsumer<UserRepository> function = repo -> repo.deleteByLogin(user.getLogin());
        repositorySupport.execute(function, message, params);
    }

    @Override
    public User registerUser(String login, String username, String password, String email, Role role) {
        User user = null;
        try {
            user = getUserByLogin(login);
        } catch (UsernameNotFoundException e) {
            //ignore
        }

        if (user != null) {
            throw new UserExistsException(String.format("User %s already exists!", username));
        }

        user = new User(login, username, password, email, role.name());
        try {
            return addUser(user);
        } catch (Exception e) {
            throw new UserServiceException(String.format("User [%s] registration error!", user), e);
        }
    }

    @Override
    public void confirmUser(String login, String verificationId, boolean isAdminUser) {
        User user = null;
        try {
            user = getUserByLogin(login);
        } catch (UsernameNotFoundException e) {
            //ignore
        }

        if (isAdminUser && (verificationId == null || verificationId.isEmpty()) && user != null) {
            verificationId = user.getVerificationId();
        }

        if (user != null && user.getVerificationId().equals(verificationId)) {
            Calendar calendar = Calendar.getInstance();
            Date currentDate = calendar.getTime();
            Date userExpirationDate = user.getExpirationDate();
            long maxExpirationMs = (long) User.EXPIRATION_DAYS * 86_400_000;
            if (currentDate.after(userExpirationDate) ||
                    Math.abs(currentDate.getTime() - userExpirationDate.getTime()) < maxExpirationMs) {
                calendar.add(Calendar.YEAR, 100);
                user.setExpirationDate(calendar.getTime());
                try {
                    updateUser(user);
                } catch (Exception e) {
                    throw new UserConfirmFailedException(e.getMessage(), e);
                }
            }
        } else {
            throw new UserConfirmFailedException("Undefined user verificationId!");
        }
    }

    @Override
    public String generateNewUserPassword(String recoveryId) {
        UserRecoveryParams userRecoveryParams = passwordRecoveryIds.get(recoveryId);
        if (userRecoveryParams.getExpirationTime().isAfter(LocalDateTime.now())) {
            String newPassword = generateRandomPassword(15);
            userRecoveryParams.getUser().setPassword(passwordEncoder.encode(newPassword));
            return newPassword;
        } else {
            throw new RecoveryExpiredException("Recovery id " + "[" + recoveryId + "] is expired");
        }
    }

    @Override
    public Map<String, User> getUserRecoveryId(String loginOrEmail) {
        List<User> users;
        String message = "Error while getting recovery id, login/email = [%s]";
        Object[] params = {loginOrEmail};
        RepositoryListFunction<UserRepository, User> function = repo -> repo.findByEmail(loginOrEmail);
        users = repositorySupport.execute(function, message, params);

        if (users.isEmpty()) {
            function = repo -> repo.findByLogin(loginOrEmail);
            users = repositorySupport.execute(function, message, params);

            if (users.isEmpty()) {
                throw new UserServiceException(String.format("User [%s] not found!", loginOrEmail));
            }
        }

        UserRecoveryParams userRecoveryParams = new UserRecoveryParams(users.get(0));
        passwordRecoveryIds.put(userRecoveryParams.getRecoveryId(), userRecoveryParams);
        return Collections.singletonMap(userRecoveryParams.getRecoveryId(), userRecoveryParams.getUser());
    }

    @Override
    public User getUserByRecoveryId(String recoveryId) {
        UserRecoveryParams userRecoveryParams = passwordRecoveryIds.get(recoveryId);
        if (userRecoveryParams == null) {
            throw new UserServiceException("User with recoveryId = " + recoveryId + " not found!");
        }
        return userRecoveryParams.getUser();
    }

    @Override
    public boolean accessGrantedForRegistration(Role role, String authorizedName) {
        boolean granted = true;

        if (role.equals(Role.ROLE_ADMIN)) {
            if (authorizedName != null && !authorizedName.isEmpty()) {
                try {
                    User admin = getUserByLogin(authorizedName);
                    if (!Role.ROLE_ADMIN.name().equals(admin.getAuthority())) {
                        granted = false;
                    }
                } catch (UsernameNotFoundException e) {
                    granted = false;
                }
            } else {
                granted = false;
            }
        }

        return granted;
    }

    @Override
    public User processOAuthPostLogin(OAuth2User oAuth2User) {
        User user = null;
        String email = oAuth2User.getAttribute("email");
        try {
            user = getUserByEmail(email);
        } catch (EmailNotFoundException e) {
            //ignore, it's ok
        }

        if (user == null) {
            user = registerUser(email, email, generateRandomPassword(10), email, Role.ROLE_USER);
            confirmUser(user.getLogin(), user.getVerificationId(), true);
        }

        return user;
    }

    @Override
    @Transactional
    public User processSuccessAuthentication(Authentication authentication, HttpServletRequest request,
                                             EventType eventType) {
        User user = null;
        try {
            OAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            user = processOAuthPostLogin(oAuth2User);
        } catch (ClassCastException e) {
            //ignore, it's ok
        }

        if (user == null) {
            user = getUserByLogin(authentication.getPrincipal().toString());
        }

        if (user == null) {
            throw new UserNotFoundException(
                    String.format("User [%s] not found!", authentication.getPrincipal().toString()));
        }

        if (user.getExpirationDate().before(new Date())) {
            deleteUser(user);
            throw new UserExpiredException(String.format("User [%s] has been expired!", user.getLogin()));
        }


        saveUserEvent(user, request, eventType);
        return user;
    }


    @Override
    public User getUserByOAuth2Token(OAuth2AuthenticationToken token) {
        OAuth2User oAuth2User = token.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        try {
            return getUserByEmail(email);
        } catch (EmailNotFoundException e) {
            return null;
        }
    }

    private EventDto saveUserEvent(User user, HttpServletRequest request,
                                   EventType eventType) {
        String message = "";
        switch (eventType) {
            case LOGGED_IN:
                message = String.format("User [%s] logged in using IP: %s", user.getLogin(), request.getRemoteAddr());
                break;
            case LOGGED_OUT:
                message = String.format("User [%s] logged out using IP: %s", user.getLogin(), request.getRemoteAddr());
                break;
        }
        return eventService.createEvent(user, eventType, message);
    }

    private static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        return IntStream.range(0, length)
                .map(i -> random.nextInt(chars.length()))
                .mapToObj(randomIndex -> String.valueOf(chars.charAt(randomIndex)))
                .collect(Collectors.joining());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getUserByLogin(username);
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

    private static final class UserRecoveryParams {
        private final User user;
        private final String recoveryId;
        private final LocalDateTime expirationTime;

        private UserRecoveryParams(User user) {
            this.user = user;
            this.recoveryId = UUID.randomUUID().toString();
            this.expirationTime = LocalDateTime.now().plusDays(1);
        }

        public User getUser() {
            return user;
        }

        public String getRecoveryId() {
            return recoveryId;
        }

        public LocalDateTime getExpirationTime() {
            return expirationTime;
        }
    }
}
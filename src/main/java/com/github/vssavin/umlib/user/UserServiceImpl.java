package com.github.vssavin.umlib.user;

import com.querydsl.core.types.Predicate;
import com.github.vssavin.umlib.config.DataSourceSwitcher;
import com.github.vssavin.umlib.email.EmailNotFoundException;
import com.github.vssavin.umlib.data.pagination.Paged;
import com.github.vssavin.umlib.data.pagination.Paging;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author vssavin on 18.12.2021
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Map<String, UserRecoveryParams> passwordRecoveryIds = new ConcurrentHashMap<>();
    private static final User EMPTY_USER = new User("", "", "", "", "");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataSourceSwitcher dataSourceSwitcher;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           DataSourceSwitcher dataSourceSwitcher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.dataSourceSwitcher = dataSourceSwitcher;
    }

    @Override
    public Paged<User> getUsers(UserFilter userFilter, int pageNumber, int size) {
        Page<User> users = null;
        Throwable throwable = null;
        dataSourceSwitcher.switchToUmDataSource();
        try {
            Pageable pageable = PageRequest.of(pageNumber - 1, size);
            if (userFilter == null || userFilter.isEmpty()) {
                users = userRepository.findAll(pageable);
            } else {
                Predicate predicate = userFilterToPredicate(userFilter);
                users = userRepository.findAll(predicate, pageable);
            }
        } catch (Exception e) {
            throwable = e;
        }

        dataSourceSwitcher.switchToPreviousDataSource();

        if (throwable != null) {
            throw new UserServiceException(
                    String.format("Error while search user with params: pageNumber = %d, size = %d, filter: [%s]!",
                            pageNumber, size, userFilter), throwable);
        }

        return new Paged<>(users, Paging.of(users.getTotalPages(), pageNumber, size));
    }

    @Override
    public User getUserById(Long id) {
        Throwable throwable = null;
        User user = null;
        dataSourceSwitcher.switchToUmDataSource();
        try {
            user = userRepository.findById(id).orElse(EMPTY_USER);
        } catch (Exception e) {
            throwable = e;
        }

        dataSourceSwitcher.switchToPreviousDataSource();

        if (throwable != null) {
            throw new UserServiceException(String.format("Getting a user by id = %d error!", id), throwable);
        }

        return user;
    }

    @Override
    public User addUser(User user) {
        User savedUser = null;
        Throwable throwable = null;
        dataSourceSwitcher.switchToUmDataSource();
        try {
            savedUser = userRepository.save(user);
        } catch (Exception e) {
            throwable = e;
        }

        dataSourceSwitcher.switchToPreviousDataSource();

        if (throwable != null) {
            throw new UserServiceException(String.format("Adding error for user [%s]!", user), throwable);
        }

        return savedUser;
    }

    @Override
    public User updateUser(User user) {
        User updatedUser = null;
        Throwable throwable = null;
        dataSourceSwitcher.switchToUmDataSource();
        try {
            updatedUser = userRepository.save(user);
        } catch (Exception e) {
            throwable = e;
        }

        dataSourceSwitcher.switchToPreviousDataSource();

        if (throwable != null) {
            throw new UserServiceException(String.format("Update error for user [%s]", user), throwable);
        }

        return updatedUser;
    }

    @Override
    public User getUserByName(String name) {
        Throwable throwable = null;
        List<User> users = null;
        dataSourceSwitcher.switchToUmDataSource();
        try {
            users = userRepository.findUserByName(name);
        } catch (Exception e) {
            throwable = e;
        }

        dataSourceSwitcher.switchToPreviousDataSource();

        if (throwable != null) {
            throw new UserServiceException(String.format("Error while getting user by name [%s]", name), throwable);
        }

        if (users != null && !users.isEmpty()) {
            return users.get(0);
        }
        throw new UsernameNotFoundException(String.format("User: %s not found!", name));
    }

    @Override
    public User getUserByLogin(String login) {
        Throwable throwable = null;
        dataSourceSwitcher.switchToUmDataSource();
        List<User> users = null;
        try {
            users = userRepository.findByLogin(login);
        } catch (Exception e) {
            throwable = e;
        }

        dataSourceSwitcher.switchToPreviousDataSource();

        if (throwable != null) {
            throw new UserServiceException(String.format("Error while getting user by login [%s]", login), throwable);
        }

        if (users != null && !users.isEmpty()) {
            return users.get(0);
        }
        throw new UsernameNotFoundException(String.format("User with login: %s not found!", login));
    }

    @Override
    public User getUserByEmail(String email) {
        Throwable throwable = null;
        dataSourceSwitcher.switchToUmDataSource();
        List<User> users = null;
        try {
            users = userRepository.findByEmail(email);
        } catch (Exception e) {
            throwable = e;
        }

        dataSourceSwitcher.switchToPreviousDataSource();
        if (throwable != null) {
            throw new UserServiceException(String.format("Error while getting user by email [%s]", email), throwable);
        }

        if (users != null && !users.isEmpty()) {
            return users.get(0);
        }

        throw new EmailNotFoundException(String.format("Email: %s not found!", email));
    }

    @Override
    public void deleteUser(User user) {
        Objects.requireNonNull(user, "User must not be null!");
        Throwable throwable = null;
        dataSourceSwitcher.switchToUmDataSource();
        try {
            userRepository.deleteByLogin(user.getLogin());
        } catch (Exception e) {
            throwable = e;
        }
        dataSourceSwitcher.switchToPreviousDataSource();
        if (throwable != null) {
            throw new UserServiceException(String.format("Error while deleting user [%s]", user), throwable);
        }
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
        Throwable throwable = null;
        dataSourceSwitcher.switchToUmDataSource();
        List<User> users = null;
        try {
            users = userRepository.findByEmail(loginOrEmail);
        } catch (Exception e) {
            throwable = e;
        }

        if (throwable != null) {
            dataSourceSwitcher.switchToPreviousDataSource();
            throw new UserServiceException(
                    String.format("Error while getting recovery id, login/email = [%s]", loginOrEmail), throwable);
        }

        if (users.isEmpty()) {
            try {
                users = userRepository.findByLogin(loginOrEmail);
            } catch (Exception e) {
                throwable = e;
            }

            if (throwable != null) {
                dataSourceSwitcher.switchToPreviousDataSource();
                throw new UserServiceException(
                        String.format("Error while getting recovery id, login/email = [%s]", loginOrEmail), throwable);
            }

        }

        if (users.isEmpty()) {
            throw new UsernameNotFoundException("Such user not found");
        }

        UserRecoveryParams userRecoveryParams = new UserRecoveryParams(users.get(0));
        passwordRecoveryIds.put(userRecoveryParams.getRecoveryId(), userRecoveryParams);
        return Collections.singletonMap(userRecoveryParams.getRecoveryId(), userRecoveryParams.getUser());
    }

    @Override
    public User getUserByRecoveryId(String recoveryId) {
        UserRecoveryParams userRecoveryParams = passwordRecoveryIds.get(recoveryId);
        if (userRecoveryParams == null) {
            throw new UsernameNotFoundException("User with recoveryId = " + recoveryId + " not found!");
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
    public User getUserByOAuth2Token(OAuth2AuthenticationToken token) {
        OAuth2User oAuth2User = token.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        try {
            return getUserByEmail(email);
        } catch (EmailNotFoundException e) {
            return null;
        }
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
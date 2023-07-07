package com.github.vssavin.umlib.user;

import com.querydsl.core.types.Predicate;
import com.github.vssavin.umlib.config.DataSourceSwitcher;
import com.github.vssavin.umlib.email.EmailNotFoundException;
import com.github.vssavin.umlib.data.pagination.Paged;
import com.github.vssavin.umlib.data.pagination.Paging;
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
        Page<User> users;
        Pageable pageable = PageRequest.of(pageNumber - 1, size);
        dataSourceSwitcher.switchToUmDataSource();
        if (userFilter == null || userFilter.isEmpty()) {
            users = userRepository.findAll(pageable);
        } else {
            Predicate predicate = UserUtils.userFilterToPredicate(userFilter);
            users = userRepository.findAll(predicate, pageable);
        }
        dataSourceSwitcher.switchToPreviousDataSource();

        return new Paged<>(users, Paging.of(users.getTotalPages(), pageNumber, size));
    }

    @Override
    public User getUserById(Long id) {
        dataSourceSwitcher.switchToUmDataSource();
        User user = userRepository.findById(id).orElse(EMPTY_USER);
        dataSourceSwitcher.switchToPreviousDataSource();
        return user;
    }

    @Override
    public User addUser(User user) {
        User savedUser;
        try {
            dataSourceSwitcher.switchToUmDataSource();
            savedUser = userRepository.save(user);
            dataSourceSwitcher.switchToPreviousDataSource();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return savedUser;
    }

    @Override
    public User updateUser(User user) {
        User updatedUser;
        try {
            dataSourceSwitcher.switchToUmDataSource();
            updatedUser = userRepository.save(user);
            dataSourceSwitcher.switchToPreviousDataSource();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return updatedUser;
    }

    @Override
    public User getUserByName(String name) {
        dataSourceSwitcher.switchToUmDataSource();
        List<User> users = userRepository.findUserByName(name);
        dataSourceSwitcher.switchToPreviousDataSource();
        if (users != null && users.size() > 0) {
            return users.get(0);
        }
        throw new UsernameNotFoundException(String.format("User: %s not found!", name));
    }

    @Override
    public User getUserByLogin(String login) {
        dataSourceSwitcher.switchToUmDataSource();
        List<User> users = userRepository.findByLogin(login);
        dataSourceSwitcher.switchToPreviousDataSource();
        if (users != null && users.size() > 0) {
            return users.get(0);
        }
        throw new UsernameNotFoundException(String.format("User with login: %s not found!", login));
    }

    @Override
    public User getUserByEmail(String email) {
        dataSourceSwitcher.switchToUmDataSource();
        List<User> users = userRepository.findByEmail(email);
        dataSourceSwitcher.switchToPreviousDataSource();
        if (users != null && users.size() > 0) {
            return users.get(0);
        }

        throw new EmailNotFoundException(String.format("Email: %s not found!", email));
    }

    @Override
    public void deleteUser(User user) {
        if (user != null) {
            dataSourceSwitcher.switchToUmDataSource();
            userRepository.deleteByLogin(user.getLogin());
            dataSourceSwitcher.switchToPreviousDataSource();
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
            throw new RuntimeException(e);
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
            long maxExpirationMs = User.EXPIRATION_DAYS * 86_400_000;
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
        dataSourceSwitcher.switchToUmDataSource();
        List<User> users = userRepository.findByEmail(loginOrEmail);
        if (users.size() == 0) {
            users = userRepository.findByLogin(loginOrEmail);
        }
        dataSourceSwitcher.switchToPreviousDataSource();
        if (users.size() == 0) {
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
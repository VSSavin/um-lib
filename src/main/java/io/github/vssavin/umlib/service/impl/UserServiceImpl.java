package io.github.vssavin.umlib.service.impl;

import io.github.vssavin.umlib.config.DataSourceSwitcher;
import io.github.vssavin.umlib.dto.UserFilter;
import io.github.vssavin.umlib.entity.Role;
import io.github.vssavin.umlib.entity.User;
import io.github.vssavin.umlib.exception.EmailNotFoundException;
import io.github.vssavin.umlib.exception.RecoveryExpiredException;
import io.github.vssavin.umlib.exception.UserConfirmFailedException;
import io.github.vssavin.umlib.exception.UserExistsException;
import io.github.vssavin.umlib.pagination.Paged;
import io.github.vssavin.umlib.pagination.Paging;
import io.github.vssavin.umlib.repository.UserRepository;
import io.github.vssavin.umlib.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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
    private final EntityManagerFactory managerFactory;
    private final DataSourceSwitcher dataSourceSwitcher;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           EntityManagerFactory managerFactory, DataSourceSwitcher dataSourceSwitcher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.managerFactory = managerFactory;
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
            EntityManager em = managerFactory.createEntityManager();
            em.getTransaction().begin();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<User> playerCriteria = cb.createQuery(User.class);
            Root<User> playerRoot = playerCriteria.from(User.class);
            playerCriteria.select(playerRoot);
            List<Predicate> predicates = new ArrayList<>();
            addPredicate(cb, playerRoot, predicates, "id", userFilter.getUserId(), Operator.EQUAL);
            addPredicate(cb, playerRoot, predicates, "login", userFilter.getLogin(), Operator.LIKE);
            addPredicate(cb, playerRoot, predicates, "name", userFilter.getName(), Operator.LIKE);
            addPredicate(cb, playerRoot, predicates, "email", userFilter.getEmail(), Operator.LIKE);
            playerCriteria.where(predicates.toArray(new Predicate[]{}));
            TypedQuery<User> query = em.createQuery(playerCriteria);
            List<User> resultList = query.getResultList();
            users = new PageImpl<>(resultList,
                    pageable, resultList.size());
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
        }
        catch (Exception e) {
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
        }
        catch (Exception e) {
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
            }
            else {
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
            user = registerUser(email, email, "", email, Role.ROLE_USER);
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

    private void addPredicate(CriteriaBuilder cb, Root<User> userRoot,
                              List<Predicate> predicates, String tableField, Object value, Operator operator) {
        if (value != null) {
            Predicate predicate = null;
            switch (operator) {
                case LIKE:
                    predicate = cb.like(userRoot.get(tableField), "%" + value + "%");
                    predicates.add(predicate);
                    break;

                case EQUAL:
                    predicate = cb.equal(userRoot.get(tableField), value);
                    predicates.add(predicate);
                    break;

                case GREATER_OR_EQUAL:
                    if (value instanceof Number) {
                        predicate = cb.greaterThanOrEqualTo(userRoot.get(tableField), ((Number) value).doubleValue());
                    }
                    else if (value instanceof Date) {
                        predicate = cb.greaterThanOrEqualTo(userRoot.get(tableField), (Date)value);
                    }
                    break;

                case LESS_OR_EQUAL:
                    if (value instanceof Number) {
                        predicate = cb.lessThanOrEqualTo(userRoot.get(tableField), ((Number) value).doubleValue());
                    }
                    else if (value instanceof Date) {
                        predicate = cb.lessThanOrEqualTo(userRoot.get(tableField), (Date)value);
                    }
                    break;
            }
            if (predicate != null) predicates.add(predicate);
        }
    }

    private enum Operator {
        EQUAL,
        LIKE,
        GREATER_OR_EQUAL,
        LESS_OR_EQUAL
    }

    private static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        return IntStream.range(0, length)
                .map(i -> random.nextInt(chars.length()))
                .mapToObj(randomIndex -> String.valueOf(chars.charAt(randomIndex)))
                .collect(Collectors.joining());
    }

    private static class UserRecoveryParams {
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

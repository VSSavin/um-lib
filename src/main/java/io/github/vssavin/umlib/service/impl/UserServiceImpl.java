package io.github.vssavin.umlib.service.impl;

import io.github.vssavin.umlib.entity.Role;
import io.github.vssavin.umlib.entity.User;
import io.github.vssavin.umlib.exception.EmailNotFoundException;
import io.github.vssavin.umlib.exception.RecoveryExpiredException;
import io.github.vssavin.umlib.exception.UserConfirmFailedException;
import io.github.vssavin.umlib.exception.UserExistsException;
import io.github.vssavin.umlib.repository.UserRepository;
import io.github.vssavin.umlib.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User addUser(User user) {
        User savedUser;
        try {
            savedUser = userRepository.save(user);
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return savedUser;
    }

    @Override
    public User updateUser(User user) {
        User updatedUser;
        try {
            updatedUser = userRepository.save(user);
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return updatedUser;
    }

    @Override
    public User getUserByName(String name) {
        List<User> users = userRepository.findByLogin(name);
        if (users != null && users.size() > 0) {
            return users.get(0);
        }
        throw new UsernameNotFoundException(String.format("User: %s not found!", name));
    }

    @Override
    public User getUserByEmail(String email) {
        List<User> users = userRepository.findByEmail(email);
        if (users != null && users.size() > 0) {
            return users.get(0);
        }

        throw new EmailNotFoundException(String.format("Email: %s not found!", email));
    }

    @Override
    public void deleteUser(User user) {
        if (user != null) {
            userRepository.deleteByLogin(user.getLogin());
        }
    }

    @Override
    public User registerUser(String login, String username, String password, String email, Role role) {
        User user = null;
        try {
            user = getUserByName(login);
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
            user = getUserByName(login);
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
            userRecoveryParams.getUser().setPassword(newPassword);
            return passwordEncoder.encode(newPassword);
        } else {
            throw new RecoveryExpiredException("Recovery id " + "[" + recoveryId + "] is expired");
        }
    }

    @Override
    public String getUserRecoveryId(String loginOrEmail) {
        List<User> users = userRepository.findByEmail(loginOrEmail);
        if (users.size() == 0) {
            users = userRepository.findByLogin(loginOrEmail);
        }
        if (users.size() == 0) {
            throw new UsernameNotFoundException("Such user not found");
        }

        UserRecoveryParams userRecoveryParams = new UserRecoveryParams(users.get(0));
        passwordRecoveryIds.put(userRecoveryParams.getRecoveryId(), userRecoveryParams);
        return userRecoveryParams.getRecoveryId();
    }

    @Override
    public boolean accessGrantedForRegistration(Role role, String authorizedName) {
        boolean granted = true;

        if (role.equals(Role.ROLE_ADMIN)) {
            if (authorizedName != null && !authorizedName.isEmpty()) {
                try {
                    User admin = getUserByName(authorizedName);
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

    private static String generateRandomPassword(int length)
    {
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
            this.recoveryId = UUID.randomUUID().toString();;
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

package io.github.vssavin.umlib.service;

import io.github.vssavin.umlib.entity.Role;
import io.github.vssavin.umlib.entity.User;
import io.github.vssavin.umlib.pagination.Paged;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author vssavin on 18.12.2021
 */
@Service
public interface UserService {
    Paged<User> getUsers(int pageNumber, int size);
    User addUser(User user) throws Exception;
    User updateUser(User user) throws Exception;
    User getUserByName(String name);
    User getUserByEmail(String email);
    void deleteUser(User user);
    User registerUser(String login, String username, String password, String email, Role role);
    void confirmUser(String login, String verificationId, boolean isAdminUser);
    String generateNewUserPassword(String recoveryId);
    Map<String, User> getUserRecoveryId(String loginOrEmail);
    User getUserByRecoveryId(String recoveryId);
    boolean accessGrantedForRegistration(Role role, String authorizedName);
}

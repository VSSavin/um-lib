package io.github.vssavin.umlib.service;

import io.github.vssavin.umlib.entity.Role;
import io.github.vssavin.umlib.entity.User;
import org.springframework.stereotype.Service;

/**
 * @author vssavin on 18.12.2021
 */
@Service
public interface UserService {
    User addUser(User user) throws Exception;
    User updateUser(User user) throws Exception;
    User getUserByName(String name);
    User getUserByEmail(String email);
    void deleteUser(User user);
    User registerUser(String login, String username, String password, String email, Role role);
    void confirmUser(String login, String verificationId, boolean isAdminUser);
    boolean accessGrantedForRegistration(Role role, String authorizedName);
}

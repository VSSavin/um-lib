package com.github.vssavin.umlib.domain.user;

import com.github.vssavin.umlib.data.pagination.Paged;
import com.github.vssavin.umlib.domain.event.EventType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Main interface for service provides user management.
 *
 * @author vssavin on 18.12.2021
 */
@Service
public interface UserService extends UserDetailsService {
    Paged<User> getUsers(UserFilter userFilter, int pageNumber, int size);
    User getUserById(Long id);
    User addUser(User user);
    User updateUser(User user);
    User getUserByName(String name);
    User getUserByLogin(String login);
    User getUserByEmail(String email);
    void deleteUser(User user);
    User registerUser(String login, String username, String password, String email, Role role);
    void confirmUser(String login, String verificationId, boolean isAdminUser);
    String generateNewUserPassword(String recoveryId);
    Map<String, User> getUserRecoveryId(String loginOrEmail);
    User getUserByRecoveryId(String recoveryId);
    boolean accessGrantedForRegistration(Role role, String authorizedName);
    User processOAuthPostLogin(OAuth2User oAuth2User);
    User processSuccessAuthentication(Authentication authentication, HttpServletRequest request, EventType eventType);
    User getUserByOAuth2Token(OAuth2AuthenticationToken token);
}

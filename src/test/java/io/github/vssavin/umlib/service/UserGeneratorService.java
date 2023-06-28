package io.github.vssavin.umlib.service;

import io.github.vssavin.umlib.user.Role;
import io.github.vssavin.umlib.user.UserExistsException;
import io.github.vssavin.umlib.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by vssavin on 04.08.2022.
 */
@Service
@DependsOn("sqlScriptsConfig")
public class UserGeneratorService {
    private final Logger log = LoggerFactory.getLogger(UserGeneratorService.class);
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserGeneratorService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void generateUsers() {
        String countUsersString = System.getProperty("userGenerator.count");
        int count = 10;

        if (countUsersString != null) {
            try {
                count = Integer.parseInt(countUsersString);
            } catch (NumberFormatException e) {
                log.error("'userGenerator.count' property should be integer number");
            }
        }

        for(int i = 0; i < count; i++) {
            String login = String.valueOf(i);
            String name = String.valueOf(i);
            String password = passwordEncoder.encode(login);
            String email = login + "@" + login + ".com";
            Role role = Role.ROLE_USER;
            try {
                userService.registerUser(login, name, password, email, role);
            } catch (UserExistsException e) {
                log.error("Register user error: ", e);
            }

        }
    }
}

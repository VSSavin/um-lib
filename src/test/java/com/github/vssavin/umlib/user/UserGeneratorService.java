package com.github.vssavin.umlib.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author vssavin on 04.08.2022.
 */
@Service
@DependsOn("sqlScriptsConfig")
class UserGeneratorService {
    private static final int DEFAULT_USERS_COUNT = 10;
    private final Logger log = LoggerFactory.getLogger(UserGeneratorService.class);
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final int countUsers;

    public UserGeneratorService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        String countUsersString = System.getProperty("userGenerator.count");
        int tmpCountUsers = DEFAULT_USERS_COUNT;
        if (countUsersString != null) {
            try {
                tmpCountUsers = Integer.parseInt(countUsersString);
            } catch (NumberFormatException e) {
                log.error("'userGenerator.count' property should be integer number");
            }
        }

        countUsers = tmpCountUsers;
    }

    @PostConstruct
    public void generateUsers() {
        for(int i = 0; i < countUsers; i++) {
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

    public UserService getUserService() {
        return userService;
    }
}

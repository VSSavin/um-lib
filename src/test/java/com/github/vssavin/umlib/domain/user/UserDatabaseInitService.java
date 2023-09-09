package com.github.vssavin.umlib.domain.user;

import com.github.vssavin.umlib.config.SqlScriptsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vssavin on 04.08.2022.
 */
@Service
public class UserDatabaseInitService {
    private static final int DEFAULT_USERS_COUNT = 10;
    private final Logger log = LoggerFactory.getLogger(UserDatabaseInitService.class);
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final SqlScriptsConfig scriptsConfig;
    private final DataSource umDataSource;
    private final int countUsers;

    public UserDatabaseInitService(UserService userService, PasswordEncoder passwordEncoder,
                                   SqlScriptsConfig scriptsConfig, DataSource umDataSource) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.scriptsConfig = scriptsConfig;
        this.umDataSource = umDataSource;
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
    public void initUserDatabase() {
        initScripts();
        for (int i = 0; i < countUsers; i++) {
            String login = String.valueOf(i);
            String name = String.valueOf(i);
            String password = passwordEncoder.encode(login);
            String email = login + "@" + login + ".com";
            Role role = Role.ROLE_USER;
            try {
                userService.registerUser(login, name, password, email, role);
            } catch (Exception e) {
                log.error("Register user error: ", e);
            }
        }
    }

    private void initScripts() {
        List<String> sourceFiles = new ArrayList<>();
        sourceFiles.add("/init_test.sql");
        scriptsConfig.executeSqlScripts(umDataSource, "", sourceFiles);
    }

    public UserService getUserService() {
        return userService;
    }
}

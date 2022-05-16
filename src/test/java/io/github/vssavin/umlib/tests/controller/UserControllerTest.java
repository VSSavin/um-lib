package io.github.vssavin.umlib.tests.controller;

import io.github.vssavin.umlib.tests.AbstractTest;
import io.github.vssavin.umlib.config.LocaleConfig;
import io.github.vssavin.umlib.controller.MessageKeys;
import io.github.vssavin.umlib.entity.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author vssavin on 07.01.2022
 */
public class UserControllerTest extends AbstractTest {

    private LocaleConfig.LocaleSpringMessageSource registrationMessageSource;
    private LocaleConfig.LocaleSpringMessageSource changePasswordMessageSource;

    private User testUser = new User("user", "user", "user",
            "user@example.com", "USER");

    @Autowired
    public void setRegistrationMessageSource(LocaleConfig.LocaleSpringMessageSource registrationMessageSource) {
        this.registrationMessageSource = registrationMessageSource;
    }

    @Autowired
    public void setChangePasswordMessageSource(LocaleConfig.LocaleSpringMessageSource changePasswordMessageSource) {
        this.changePasswordMessageSource = changePasswordMessageSource;
    }

    @Test
    public void suchUserExists() throws Exception {
        MultiValueMap<String, String> registerParams = new LinkedMultiValueMap<>();
        String login = testUser.getLogin();
        registerParams.add("login", login);
        registerParams.add("username", login);
        registerParams.add("email", "test@test.com");
        registerParams.add("password", testUser.getPassword());
        registerParams.add("confirmPassword", testUser.getPassword());
        ResultActions resultActions = mockMvc.perform(post("/user/perform-register/")
                        .params(registerParams)
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));
        String messagePattern = registrationMessageSource.getMessage(MessageKeys.USER_EXISTS_PATTERN.getMessageKey(),
                new Object[]{}, LocaleConfig.DEFAULT_LOCALE);
        resultActions.andExpect(model().attribute("error", true))
                .andExpect(model().attribute("errorMsg", String.format(messagePattern, login)))
                .andExpect(status().is(302));
    }

    @Test
    public void suchEmailExists() throws Exception {
        MultiValueMap<String, String> registerParams = new LinkedMultiValueMap<>();
        String login = "user3";
        registerParams.add("login", login);
        registerParams.add("username", login);
        registerParams.add("email", "user@example.com");
        registerParams.add("password", testUser.getPassword());
        registerParams.add("confirmPassword", testUser.getPassword());
        ResultActions resultActions = mockMvc.perform(post("/user/perform-register/")
                .params(registerParams)
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));
        String messagePattern = registrationMessageSource.getMessage(MessageKeys.EMAIL_EXISTS_MESSAGE.getMessageKey(),
                new Object[]{}, LocaleConfig.DEFAULT_LOCALE);
        resultActions.andExpect(model().attribute("error", true))
                .andExpect(model().attribute("errorMsg", String.format(messagePattern, login)))
                .andExpect(status().is(302));
    }

    @Test
    public void changeUserPasswordSuccessful() throws Exception {
        String currentPassword = testUser.getPassword();
        String newPassword = "user2";

        ResultActions secureAction = mockMvc.perform(get("/secure/key"));
        String secureKey = secureAction.andReturn().getResponse().getContentAsString();
        String encodedCurrentPassword = secureService.encrypt(currentPassword, secureKey);
        String encodedNewPassword = secureService.encrypt(newPassword, secureKey);
        MultiValueMap<String, String> registerParams = new LinkedMultiValueMap<>();
        registerParams.add("currentPassword", encodedCurrentPassword);
        registerParams.add("newPassword", encodedNewPassword);

        ResultActions resultActions = mockMvc.perform(post("/user/perform-change-password/")
                .params(registerParams)
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));

        String message = changePasswordMessageSource.getMessage(
                MessageKeys.PASSWORD_SUCCESSFULLY_CHANGED_MESSAGE.getMessageKey(), new Object[]{},
                LocaleConfig.DEFAULT_LOCALE);
        resultActions.andExpect(model().attribute("success", true))
                .andExpect(model().attribute("successMsg", message));
        testUser.setPassword(newPassword);
    }

    @Test
    public void changeUserPasswordFailed() throws Exception {
        String currentPassword = testUser.getPassword() + "1";
        String newPassword = "admin2";

        ResultActions secureAction = mockMvc.perform(get("/secure/key"));
        String secureKey = secureAction.andReturn().getResponse().getContentAsString();
        String encodedCurrentPassword = secureService.encrypt(currentPassword, secureKey);
        String encodedNewPassword = secureService.encrypt(newPassword, secureKey);

        MultiValueMap<String, String> registerParams = new LinkedMultiValueMap<>();
        registerParams.add("currentPassword", encodedCurrentPassword);
        registerParams.add("newPassword", encodedNewPassword);

        ResultActions resultActions = mockMvc.perform(post("/user/perform-change-password/")
                .params(registerParams)
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));
        String message = changePasswordMessageSource.getMessage(
                MessageKeys.WRONG_PASSWORD_MESSAGE.getMessageKey(), new Object[]{},
                LocaleConfig.DEFAULT_LOCALE);
        resultActions.andExpect(model().attribute("error", true))
                .andExpect(model().attribute("errorMsg", message));
    }

    @Test
    public void registerUserSuccessful() throws Exception {
        MultiValueMap<String, String> registerParams = new LinkedMultiValueMap<>();
        String login = "user2";

        ResultActions secureAction = mockMvc.perform(get("/secure/key"));
        String secureKey = secureAction.andReturn().getResponse().getContentAsString();
        String encodedPassword = secureService.encrypt("user2", secureKey);

        registerParams.add("login", login);
        registerParams.add("username", "user2");
        registerParams.add("email", "user2@example.com");
        registerParams.add("password", encodedPassword);
        registerParams.add("confirmPassword", encodedPassword);

        ResultActions resultActions = mockMvc.perform(post("/user/perform-register/")
                .params(registerParams)
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));
        String message = registrationMessageSource.getMessage(
                MessageKeys.USER_CREATED_SUCCESSFULLY_PATTERN.getMessageKey(), new Object[]{},
                LocaleConfig.DEFAULT_LOCALE);
        resultActions.andExpect(model().attribute("success", true))
                .andExpect(model().attribute("successMsg", String.format(message, login)));
    }
}

package io.github.vssavin.umlib.tests.controller;

import io.github.vssavin.umlib.service.MockedEmailService;
import io.github.vssavin.umlib.tests.AbstractTest;
import io.github.vssavin.umlib.config.LocaleConfig;
import io.github.vssavin.umlib.controller.MessageKeys;
import io.github.vssavin.umlib.entity.User;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

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

    private final User testUser = new User("user", "user", "user",
            "user@example.com", "USER");

    private MockedEmailService mockedEmailService;

    @Autowired
    public void setRegistrationMessageSource(LocaleConfig.LocaleSpringMessageSource registrationMessageSource) {
        this.registrationMessageSource = registrationMessageSource;
    }

    @Autowired
    public void setChangePasswordMessageSource(LocaleConfig.LocaleSpringMessageSource changePasswordMessageSource) {
        this.changePasswordMessageSource = changePasswordMessageSource;
    }

    @Autowired
    public void setEmailService(MockedEmailService emailService) {
        this.mockedEmailService = emailService;
    }

    @Test
    public void registrationNotAllowedForAuthenticatedUser() throws Exception{
        MultiValueMap<String, String> registerParams = new LinkedMultiValueMap<>();
        String login = "user2";
        String encodedPassword = encrypt("", "user2");

        registerParams.add("login", login);
        registerParams.add("username", "user2");
        registerParams.add("email", "user2@example.com");
        registerParams.add("password", encodedPassword);
        registerParams.add("confirmPassword", encodedPassword);

        ResultActions resultActions = mockMvc.perform(post("/user/perform-register")
                .params(registerParams)
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));

        resultActions.andExpect(status().is(403));
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
        ResultActions resultActions = mockMvc.perform(post("/user/perform-register")
                        .params(registerParams)
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
        ResultActions resultActions = mockMvc.perform(post("/user/perform-register")
                .params(registerParams)
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

        String encodedCurrentPassword = encrypt("", currentPassword);
        String encodedNewPassword = encrypt("", newPassword);

        MultiValueMap<String, String> registerParams = new LinkedMultiValueMap<>();
        registerParams.add("currentPassword", encodedCurrentPassword);
        registerParams.add("newPassword", encodedNewPassword);

        ResultActions resultActions = mockMvc.perform(post("/user/perform-change-password")
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

        String encodedCurrentPassword = encrypt("", currentPassword);
        String encodedNewPassword = encrypt("", newPassword);

        MultiValueMap<String, String> registerParams = new LinkedMultiValueMap<>();
        registerParams.add("currentPassword", encodedCurrentPassword);
        registerParams.add("newPassword", encodedNewPassword);

        ResultActions resultActions = mockMvc.perform(post("/user/perform-change-password")
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

        String encodedPassword = encrypt("", "user2");

        registerParams.add("login", login);
        registerParams.add("username", "user2");
        registerParams.add("email", "user2@example.com");
        registerParams.add("password", encodedPassword);
        registerParams.add("confirmPassword", encodedPassword);

        ResultActions resultActions = mockMvc.perform(post("/user/perform-register")
                .params(registerParams)
                .with(csrf()));
        String message = registrationMessageSource.getMessage(
                MessageKeys.USER_CREATED_SUCCESSFULLY_PATTERN.getMessageKey(), new Object[]{},
                LocaleConfig.DEFAULT_LOCALE);
        resultActions.andExpect(model().attribute("success", true))
                .andExpect(model().attribute("successMsg", String.format(message, login)));
    }

    @Test
    public void recoveryPasswordSuccessful() throws Exception {
        MultiValueMap<String, String> passwordRecoveryParams = new LinkedMultiValueMap<>();
        passwordRecoveryParams.add("loginOrEmail", "admin");
        mockedEmailService.getEmailMessages().clear();
        ResultActions resultActions = mockMvc.perform(post("/user/perform-password-recovery")
                .params(passwordRecoveryParams)
                .with(csrf()));
        resultActions.andExpect(model().attribute("successSend", true));

        MockedEmailService.EmailMessage emailMessage = mockedEmailService.getLastEmailMessage();
        String messageText = emailMessage.getText();
        messageText = messageText.substring(messageText.indexOf("http"));
        MultiValueMap<String, String> parameters =
                UriComponentsBuilder.fromUriString(messageText).build().getQueryParams();
        List<String> param = parameters.get("recoveryId");
        Assertions.assertEquals(1, param.size());

        String recoveryId = param.get(0);
        mockedEmailService.getEmailMessages().clear();
        passwordRecoveryParams.clear();
        passwordRecoveryParams.add("recoveryId", recoveryId);
        resultActions = mockMvc.perform(get("/user/passwordRecovery")
                .params(passwordRecoveryParams)
                .with(csrf()));
        resultActions.andExpect(model().attribute("successSend", true));

        emailMessage = mockedEmailService.getLastEmailMessage();
        messageText = emailMessage.getText();
        Assertions.assertFalse(messageText.isEmpty());
    }

    @Test
    public void recoveryPasswordFailed() throws Exception {
        MultiValueMap<String, String> passwordRecoveryParams = new LinkedMultiValueMap<>();
        passwordRecoveryParams.add("loginOrEmail", "12345");
        mockedEmailService.getEmailMessages().clear();
        ResultActions resultActions = mockMvc.perform(post("/user/perform-password-recovery")
                .params(passwordRecoveryParams)
                .with(csrf()));
        resultActions.andExpect(model().attribute("userNotFound", true));

        passwordRecoveryParams = new LinkedMultiValueMap<>();
        passwordRecoveryParams.add("loginOrEmail", "admin");
        mockedEmailService.getEmailMessages().clear();
        resultActions = mockMvc.perform(post("/user/perform-password-recovery")
                .params(passwordRecoveryParams)
                .with(csrf()));
        resultActions.andExpect(model().attribute("successSend", true));

        MockedEmailService.EmailMessage emailMessage = mockedEmailService.getLastEmailMessage();
        String messageText = emailMessage.getText();
        messageText = messageText.substring(messageText.indexOf("http"));
        MultiValueMap<String, String> parameters =
                UriComponentsBuilder.fromUriString(messageText).build().getQueryParams();
        List<String> param = parameters.get("recoveryId");
        Assertions.assertEquals(1, param.size());

        String recoveryId = "-----";
        mockedEmailService.getEmailMessages().clear();
        passwordRecoveryParams = new LinkedMultiValueMap<>();
        passwordRecoveryParams.add("recoveryId", recoveryId);
        resultActions = mockMvc.perform(get("/user/passwordRecovery")
                .params(passwordRecoveryParams)
                .with(csrf()));
        resultActions.andExpect(model().attribute("userNotFound", true))
                .andExpect(model().attribute("successSend", false));
    }

    @Test
    public void userEditWithWrongIdFailed() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", "12345");
        ResultActions resultActions = mockMvc.perform(post("/user/edit/perform-user-edit")
                .params(params)
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));
        ModelAndView modelAndView = resultActions.andReturn().getModelAndView();
        Assertions.assertNotNull(modelAndView);
        boolean error = modelAndView.getModel().containsKey("error");
        Assertions.assertTrue(error);
    }

    @Test
    public void userEditWithWrongEmailFailed() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", "2");
        params.add("name", "testName");
        params.add("email", "testEmail.com");
        ResultActions resultActions = mockMvc.perform(post("/user/edit/perform-user-edit")
                .params(params)
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));
        ModelAndView modelAndView = resultActions.andReturn().getModelAndView();
        Assertions.assertNotNull(modelAndView);
        boolean error = modelAndView.getModel().containsKey("error");
        Assertions.assertTrue(error);
    }

    @Test
    public void userEditSuccess() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", "2");
        params.add("name", "testName");
        params.add("email", "testEmail@mail.com");
        ResultActions resultActions = mockMvc.perform(post("/user/edit/perform-user-edit")
                .params(params)
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));

        ModelAndView modelAndView = resultActions.andReturn().getModelAndView();
        Assertions.assertNotNull(modelAndView);
        boolean success = modelAndView.getModel().containsKey("success");
        Assertions.assertTrue(success);
    }
}

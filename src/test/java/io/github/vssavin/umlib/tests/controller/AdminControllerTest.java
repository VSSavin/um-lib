package io.github.vssavin.umlib.tests.controller;

import io.github.vssavin.umlib.config.LocaleConfig;
import io.github.vssavin.umlib.controller.MessageKeys;
import io.github.vssavin.umlib.entity.User;
import io.github.vssavin.umlib.tests.AbstractTest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
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
 * @author vssavin on 08.01.2022
 */
public class AdminControllerTest extends AbstractTest {

    private LocaleConfig.LocaleSpringMessageSource registrationMessageSource;
    private LocaleConfig.LocaleSpringMessageSource changeUserPasswordMessageSource;

    private final User testUser = new User("admin", "admin", "admin",
            "admin@example.com", "ADMIN");

    @Autowired
    public void setRegistrationMessageSource(LocaleConfig.LocaleSpringMessageSource registrationMessageSource) {
        this.registrationMessageSource = registrationMessageSource;
    }

    @Autowired
    public void setChangeUserPasswordMessageSource(
            LocaleConfig.LocaleSpringMessageSource changeUserPasswordMessageSource) {
        this.changeUserPasswordMessageSource = changeUserPasswordMessageSource;
    }

    @Test
    public void registerUserSuccessful() throws Exception {
        MultiValueMap<String, String> registerParams = new LinkedMultiValueMap<>();
        ResultActions secureAction = mockMvc.perform(get("/secure/key"));
        String secureKey = secureAction.andReturn().getResponse().getContentAsString();
        String encodedPassword = secureService.encrypt("user2", secureKey);

        String login = "user2";
        registerParams.add("login", login);
        registerParams.add("username", login);
        registerParams.add("email", "user2@example.com");
        registerParams.add("password", encodedPassword);
        registerParams.add("confirmPassword", encodedPassword);
        ResultActions resultActions = mockMvc.perform(post("/admin/perform-register/")
                .params(registerParams)
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));
        String messagePattern = registrationMessageSource.getMessage(
                MessageKeys.USER_CREATED_SUCCESSFULLY_PATTERN.getMessageKey(), new Object[]{},
                LocaleConfig.DEFAULT_LOCALE);
        resultActions.andExpect(model().attribute("success", true))
                .andExpect(model().attribute("successMsg", String.format(messagePattern, login)))
                .andExpect(status().is(302));
    }

    @Test
    public void changeUserPasswordSuccessful() throws Exception {
        String newPassword = "admin2";
        MultiValueMap<String, String> registerParams = new LinkedMultiValueMap<>();
        ResultActions secureAction = mockMvc.perform(get("/secure/key"));
        String secureKey = secureAction.andReturn().getResponse().getContentAsString();
        String encodedNewPassword = secureService.encrypt(newPassword, secureKey);

        registerParams.add("userName", testUser.getLogin());
        registerParams.add("newPassword", encodedNewPassword);

        ResultActions resultActions = mockMvc.perform(post("/admin/perform-change-user-password/")
                .params(registerParams)
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));
        String message = changeUserPasswordMessageSource.getMessage(
                MessageKeys.PASSWORD_SUCCESSFULLY_CHANGED_MESSAGE.getMessageKey(), new Object[]{},
                LocaleConfig.DEFAULT_LOCALE);
        resultActions.andExpect(model().attribute("success", true))
                .andExpect(model().attribute("successMsg", message));
        testUser.setPassword(newPassword);
    }

    @Test
    public void userFilteringTest() throws Exception {
        MultiValueMap<String, String> registerParams = new LinkedMultiValueMap<>();
        registerParams.add("login", testUser.getLogin());

        ResultActions resultActions = mockMvc.perform(get("/admin/users/")
                .params(registerParams)
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));

        String html = resultActions.andReturn().getResponse().getContentAsString();
        Document doc = Jsoup.parse(html);
        Element usersTable = doc.getElementById("usersTable");
        Elements trElements = usersTable.getElementsByTag("tbody")
                .first().getElementsByTag("tr");
        Assertions.assertEquals(1, trElements.size());

    }
}

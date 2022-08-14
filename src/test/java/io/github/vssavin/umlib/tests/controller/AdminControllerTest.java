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
import org.springframework.web.servlet.ModelAndView;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        String encodedPassword = encrypt("", "user2");

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
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        String encodedNewPassword = encrypt("", newPassword);

        params.add("userName", testUser.getLogin());
        params.add("newPassword", encodedNewPassword);

        ResultActions resultActions = mockMvc.perform(patch("/admin/changeUserPassword")
                .params(params)
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
    public void changeUserPasswordFailedUserNotFound() throws Exception {
        String userName = "UserNotFoundName";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        params.add("userName", userName);
        params.add("newPassword", encrypt("", testUser.getPassword()));

        ResultActions resultActions = mockMvc.perform(patch("/admin/changeUserPassword")
                .params(params)
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));
        String message = changeUserPasswordMessageSource.getMessage(
                MessageKeys.USER_NOT_FOUND_MESSAGE.getMessageKey(), new Object[]{},
                LocaleConfig.DEFAULT_LOCALE);
        resultActions.andExpect(model().attribute("error", true))
                .andExpect(model().attribute("errorMsg", message));
    }

    @Test
    public void userFilteringTest() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("login", testUser.getLogin());

        ResultActions resultActions = mockMvc.perform(get("/admin/users/")
                .params(params)
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));

        String html = resultActions.andReturn().getResponse().getContentAsString();
        Document doc = Jsoup.parse(html);
        Element usersTable = doc.getElementById("usersTable");
        Elements trElements = usersTable.getElementsByTag("tbody")
                .first().getElementsByTag("tr");
        Assertions.assertEquals(1, trElements.size());

    }

    @Test
    public void editUserSuccessful() throws Exception {
        String newUserEmail = "test@test.com";
        String userLogin = "user";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("login", "user");

        ResultActions resultActions = mockMvc.perform(get("/admin/users/")
                .params(params)
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));

        String html = resultActions.andReturn().getResponse().getContentAsString();
        Document doc = Jsoup.parse(html);
        Element usersTable = doc.getElementById("usersTable");
        Elements trElements = usersTable.getElementsByTag("tbody")
                .first().getElementsByTag("tr");
        Element userElement = trElements.get(0);
        String userId = userElement.getElementsByTag("td").get(0).text();

        params = new LinkedMultiValueMap<>();
        params.add("id", userId);
        params.add("login", userLogin);
        params.add("name", "name");
        params.add("email", newUserEmail);
        resultActions = mockMvc.perform(patch("/admin/users")
                .params(params)
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));

        ModelAndView modelAndView = resultActions.andReturn().getModelAndView();

        Assertions.assertNotNull(modelAndView);
        boolean success = modelAndView.getModel().containsKey("success");
        Assertions.assertTrue(success);

        params = new LinkedMultiValueMap<>();
        params.add("login", "user");

        resultActions = mockMvc.perform(get("/admin/users/")
                .params(params)
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));

        html = resultActions.andReturn().getResponse().getContentAsString();
        doc = Jsoup.parse(html);
        usersTable = doc.getElementById("usersTable");
        trElements = usersTable.getElementsByTag("tbody")
                .first().getElementsByTag("tr");
        userElement = trElements.get(0);
        String userEmail = userElement.getElementsByTag("td").get(3).text();

        Assertions.assertEquals(newUserEmail, userEmail);
    }

    @Test
    public void editUserFailedWrongEmail() throws Exception {
        String newUserEmail = "test";
        String userLogin = "user";

        ResultActions resultActions = mockMvc.perform(get("/admin/users/")
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));

        String html = resultActions.andReturn().getResponse().getContentAsString();
        Document doc = Jsoup.parse(html);
        Element usersTable = doc.getElementById("usersTable");
        Elements trElements = usersTable.getElementsByTag("tbody")
                .first().getElementsByTag("tr");
        Element userElement = trElements.get(0);
        String userId = userElement.getElementsByTag("td").get(0).text();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", userId);
        params.add("login", userLogin);
        params.add("name", "name");
        params.add("email", newUserEmail);
        resultActions = mockMvc.perform(patch("/admin/users")
                .params(params)
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));

        ModelAndView modelAndView = resultActions.andReturn().getModelAndView();

        Assertions.assertNotNull(modelAndView);
        boolean error = modelAndView.getModel().containsKey("error");
        Assertions.assertTrue(error);
    }

    @Test
    public void editUserFailedSuchLoginExists() throws Exception {
        String newUserLogin = "user_new";

        ResultActions resultActions = mockMvc.perform(get("/admin/users/")
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));

        String html = resultActions.andReturn().getResponse().getContentAsString();
        Document doc = Jsoup.parse(html);
        Element usersTable = doc.getElementById("usersTable");
        Elements trElements = usersTable.getElementsByTag("tbody")
                .first().getElementsByTag("tr");
        Element userElement = trElements.get(1);
        String userId = userElement.getElementsByTag("td").get(0).text();
        String userEmail = userElement.getElementsByTag("td").get(3).text();
        String userName = userElement.getElementsByTag("td").get(2).text();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", userId);
        params.add("login", newUserLogin);
        params.add("name", userName);
        params.add("email", userEmail);
        resultActions = mockMvc.perform(patch("/admin/users")
                .params(params)
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));

        ModelAndView modelAndView = resultActions.andReturn().getModelAndView();

        Assertions.assertNotNull(modelAndView);
        boolean error = modelAndView.getModel().containsKey("error");
        Assertions.assertTrue(error);
    }

    @Test
    public void deleteUserSuccessful() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/admin/users/")
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));

        String html = resultActions.andReturn().getResponse().getContentAsString();
        Document doc = Jsoup.parse(html);
        Element usersTable = doc.getElementById("usersTable");
        Elements trElements = usersTable.getElementsByTag("tbody")
                .last().getElementsByTag("tr");
        Assertions.assertTrue(trElements.size() > 0);

        Element userElement = trElements.get(0);
        String userId = userElement.getElementsByTag("td").get(0).text();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", userId);
        resultActions = mockMvc.perform(delete("/admin/users")
                .params(params)
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));

        ModelAndView modelAndView = resultActions.andReturn().getModelAndView();

        Assertions.assertNotNull(modelAndView);
        boolean error = modelAndView.getModel().containsKey("error");
        Assertions.assertFalse(error);
    }

    @Test
    public void deleteUserFailed() throws Exception {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", "-1");
        ResultActions resultActions = mockMvc.perform(delete("/admin/users")
                .params(params)
                .with(getRequestPostProcessorForUser(testUser))
                .with(csrf()));

        ModelAndView modelAndView = resultActions.andReturn().getModelAndView();

        Assertions.assertNotNull(modelAndView);
        boolean error = modelAndView.getModel().containsKey("error");
        Assertions.assertTrue(error);
    }
}

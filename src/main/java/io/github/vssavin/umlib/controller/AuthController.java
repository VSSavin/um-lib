package io.github.vssavin.umlib.controller;

import io.github.vssavin.umlib.config.LocaleConfig;
import io.github.vssavin.umlib.config.UmConfig;
import io.github.vssavin.umlib.helper.MvcHelper;
import io.github.vssavin.umlib.language.UmLanguage;
import io.github.vssavin.umlib.service.SecureService;
import io.github.vssavin.umlib.service.UserService;
import io.github.vssavin.umlib.utils.UmUtil;
import io.github.vssavin.umlib.utils.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * @author vssavin on 18.12.2021
 */
@Controller
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final String PAGE_LOGIN = UmConfig.LOGIN_URL.replaceAll("/", "");
    private static final String PAGE_LOGOUT = UmConfig.LOGOUT_URL.replaceAll("/", "");

    private static final String PERFORM_LOGOUT = "/perform-logout";

    private final Set<String> PAGE_LOGIN_PARAMS;
    private final Set<String> PAGE_LOGOUT_PARAMS;

    private final UmLanguage language;
    private final SecureService secureService;
    private final UmConfig umConfig;
    private final UserService userService;

    public AuthController(LocaleConfig.LocaleSpringMessageSource loginMessageSource,
                          LocaleConfig.LocaleSpringMessageSource logoutMessageSource,
                          UmLanguage language, UmUtil umUtil, UmConfig umConfig, UserService userService) {
        this.language = language;
        this.secureService = umUtil.getAuthService();
        this.umConfig = umConfig;
        PAGE_LOGIN_PARAMS = loginMessageSource.getKeys();
        PAGE_LOGOUT_PARAMS = logoutMessageSource.getKeys();
        this.userService = userService;
    }

    @GetMapping(value = {"/", UmConfig.LOGIN_URL, UmConfig.LOGIN_URL + ".html"})
    public ModelAndView getLogin(HttpServletRequest request, HttpServletResponse response,
                                 @RequestParam(required = false) final Boolean error,
                                 @RequestParam(required = false) final String lang) {
        try {
            if (UserUtils.isAuthorizedAdmin(request, userService)) {
                response.sendRedirect(UmConfig.adminSuccessUrl);
            } else if (UserUtils.isAuthorizedUser(request, userService)) {
                response.sendRedirect(UmConfig.successUrl);
            }
        } catch (IOException e) {
            log.error("Sending redirect i/o error: ", e);
        }

        ModelAndView modelAndView = new ModelAndView(PAGE_LOGIN);
        MvcHelper.addObjectsToModelAndView(modelAndView, PAGE_LOGIN_PARAMS, language,
                secureService.getEncryptMethodNameForView(), lang);
        modelAndView.addObject("registrationAllowed", umConfig.getRegistrationAllowed());
        return modelAndView;
    }

    @GetMapping(value = {UmConfig.LOGOUT_URL, UmConfig.LOGOUT_URL + ".html"})
    public ModelAndView getLogout(@RequestParam(required = false) final String lang) {

        ModelAndView modelAndView = new ModelAndView(PAGE_LOGOUT);
        MvcHelper.addObjectsToModelAndView(modelAndView, PAGE_LOGOUT_PARAMS, language,
                secureService.getEncryptMethodNameForView(), lang);
        return modelAndView;
    }

    @PostMapping(value = PERFORM_LOGOUT)
    public ModelAndView logout(@RequestParam(required = false) final String lang) {
        String language = lang != null ? "?lang=" + lang : "";
        return new ModelAndView("redirect:" + UmConfig.LOGIN_URL + language);
    }
}

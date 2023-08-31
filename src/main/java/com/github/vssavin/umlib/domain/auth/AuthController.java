package com.github.vssavin.umlib.domain.auth;

import com.github.vssavin.umlib.base.controller.UmControllerBase;
import com.github.vssavin.umlib.config.LocaleConfig;
import com.github.vssavin.umlib.config.OAuth2Config;
import com.github.vssavin.umlib.config.UmConfig;
import com.github.vssavin.umlib.domain.language.UmLanguage;
import com.github.vssavin.umlib.domain.security.SecureService;
import com.github.vssavin.umlib.domain.user.UserSecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authentication controller - allows user authentication
 *
 * @author vssavin on 18.12.2021
 */
@Controller
final class AuthController extends UmControllerBase {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final String PAGE_LOGIN = UmConfig.LOGIN_URL.replace("/", "");
    private static final String PAGE_LOGOUT = UmConfig.LOGOUT_URL.replace("/", "");

    private static final String PERFORM_LOGOUT = "/perform-logout";

    private final Set<String> pageLoginParams;
    private final Set<String> pageLogoutParams;

    private final SecureService secureService;
    private final UserSecurityService userSecurityService;
    private final OAuth2Config oAuth2Config;

    @Autowired
    AuthController(LocaleConfig localeConfig, UmLanguage language, UmConfig umConfig,
                   UserSecurityService userSecurityService, OAuth2Config oAuth2Config) {
        super(language, umConfig);
        this.secureService = umConfig.getSecureService();
        pageLoginParams = localeConfig.forPage(PAGE_LOGIN).getKeys();
        pageLogoutParams = localeConfig.forPage(PAGE_LOGOUT).getKeys();
        this.userSecurityService = userSecurityService;
        this.oAuth2Config = oAuth2Config;
    }

    @GetMapping(value = {"/", UmConfig.LOGIN_URL, UmConfig.LOGIN_URL + ".html"})
    ModelAndView getLogin(final HttpServletRequest request, final HttpServletResponse response,
                          @RequestParam(required = false) final String lang) {
        try {
            if (userSecurityService.isAuthorizedAdmin(request)) {
                response.sendRedirect(umConfig.getAdminSuccessUrl());
            } else if (userSecurityService.isAuthorizedUser(request)) {
                response.sendRedirect(umConfig.getSuccessUrl());
            }
        } catch (IOException e) {
            log.error("Sending redirect i/o error: ", e);
        }

        ModelAndView modelAndView = new ModelAndView(PAGE_LOGIN);
        addObjectsToModelAndView(modelAndView, pageLoginParams, secureService.getEncryptMethodName(), lang);
        modelAndView.addObject("registrationAllowed", umConfig.isRegistrationAllowed());
        modelAndView.addObject("googleAuthAllowed", !("".equals(oAuth2Config.getGoogleClientId())));
        modelAndView.addObject("loginTitle", umConfig.getLoginTitle());
        modelAndView.addObject("secureScripts", normalizeScriptNames(secureService.getScriptsList()));
        return modelAndView;
    }

    @GetMapping(value = {UmConfig.LOGOUT_URL, UmConfig.LOGOUT_URL + ".html"})
    ModelAndView getLogout(@RequestParam(required = false) final String lang) {

        ModelAndView modelAndView = new ModelAndView(PAGE_LOGOUT);
        addObjectsToModelAndView(modelAndView, pageLogoutParams, secureService.getEncryptMethodName(), lang);
        return modelAndView;
    }

    @PostMapping(value = PERFORM_LOGOUT)
    ModelAndView logout(@RequestParam(required = false) final String lang) {
        String language = lang != null ? "?lang=" + lang : "";
        return new ModelAndView("redirect:" + UmConfig.LOGIN_URL + language);
    }

    private List<String> normalizeScriptNames(List<String> scripts) {
        String jsPrefix = "/js";
        return scripts.stream().map(scriptName ->
                scriptName.substring(scriptName.indexOf(jsPrefix))).collect(Collectors.toList());
    }
}

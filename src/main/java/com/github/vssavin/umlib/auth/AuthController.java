package com.github.vssavin.umlib.auth;

import com.github.vssavin.umlib.base.UmControllerBase;
import com.github.vssavin.umlib.config.LocaleConfig;
import com.github.vssavin.umlib.config.OAuth2Config;
import com.github.vssavin.umlib.config.UmConfig;
import com.github.vssavin.umlib.language.UmLanguage;
import com.github.vssavin.umlib.security.SecureService;
import com.github.vssavin.umlib.user.UserService;
import com.github.vssavin.umlib.user.UserUtils;
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
final class AuthController extends UmControllerBase {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final String PAGE_LOGIN = UmConfig.LOGIN_URL.replaceAll("/", "");
    private static final String PAGE_LOGOUT = UmConfig.LOGOUT_URL.replaceAll("/", "");

    private static final String PERFORM_LOGOUT = "/perform-logout";

    private final Set<String> pageLoginParams;
    private final Set<String> pageLogoutParams;

    private final SecureService secureService;
    private final UmConfig umConfig;
    private final UserService userService;
    private final OAuth2Config oAuth2Config;

    AuthController(LocaleConfig localeConfig, UmLanguage language, UmConfig umConfig,
                   UserService userService, OAuth2Config oAuth2Config) {
        super(language);
        this.secureService = umConfig.getAuthService();
        this.umConfig = umConfig;
        pageLoginParams = localeConfig.forPage(PAGE_LOGIN).getKeys();
        pageLogoutParams = localeConfig.forPage(PAGE_LOGOUT).getKeys();
        this.userService = userService;
        this.oAuth2Config = oAuth2Config;
    }

    @GetMapping(value = {"/", UmConfig.LOGIN_URL, UmConfig.LOGIN_URL + ".html"})
    ModelAndView getLogin(final HttpServletRequest request, final HttpServletResponse response,
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
        addObjectsToModelAndView(modelAndView, pageLoginParams, secureService.getEncryptMethodNameForView(), lang);
        modelAndView.addObject("registrationAllowed", umConfig.getRegistrationAllowed());
        modelAndView.addObject("googleAuthAllowed", !("".equals(oAuth2Config.getGoogleClientId())));
        modelAndView.addObject("loginTitle", umConfig.getLoginTitle());
        return modelAndView;
    }

    @GetMapping(value = {UmConfig.LOGOUT_URL, UmConfig.LOGOUT_URL + ".html"})
    ModelAndView getLogout(@RequestParam(required = false) final String lang) {

        ModelAndView modelAndView = new ModelAndView(PAGE_LOGOUT);
        addObjectsToModelAndView(modelAndView, pageLogoutParams, secureService.getEncryptMethodNameForView(), lang);
        return modelAndView;
    }

    @PostMapping(value = PERFORM_LOGOUT)
    ModelAndView logout(@RequestParam(required = false) final String lang) {
        String language = lang != null ? "?lang=" + lang : "";
        return new ModelAndView("redirect:" + UmConfig.LOGIN_URL + language);
    }
}

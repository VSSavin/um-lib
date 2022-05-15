package io.github.vssavin.umlib.controller;

import io.github.vssavin.umlib.config.LocaleConfig;
import io.github.vssavin.umlib.entity.Role;
import io.github.vssavin.umlib.entity.User;
import io.github.vssavin.umlib.exception.EmailNotFoundException;
import io.github.vssavin.umlib.exception.UserExistsException;
import io.github.vssavin.umlib.helper.ValidatingHelper;
import io.github.vssavin.umlib.language.UmLanguage;
import io.github.vssavin.umlib.service.SecureService;
import io.github.vssavin.umlib.service.UserService;
import io.github.vssavin.umlib.utils.UmUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Set;

import static io.github.vssavin.umlib.helper.MvcHelper.*;

/**
 * @author vssavin on 21.12.21
 */
@RestController
@RequestMapping("/admin")
public class AdminController {
    private static final String PAGE_ADMIN = "admin";
    private static final String PAGE_REGISTRATION = "registration";
    private static final String PAGE_CHANGE_USER_PASSWORD = "changeUserPassword";
    private static final String PAGE_CONFIRM_USER = "adminConfirmUser";

    private static final String PERFORM_REGISTER_MAPPING = "/perform-register";
    private static final String PERFORM_CHANGE_USER_PASSWORD = "/perform-change-user-password";

    private static final Set<String> IGNORED_PARAMS = new HashSet<>();

    static {
        IGNORED_PARAMS.add("_csrf");
        IGNORED_PARAMS.add("newPassword");
    }

    private Set<String> pageAdminParams;
    private Set<String> pageRegistrationParams;
    private Set<String> pageChangeUserPasswordParams;
    private Set<String> pageAdminConfirmUserParams;

    private UserService userService;
    private SecureService secureService;
    private PasswordEncoder passwordEncoder;
    private UmLanguage language;

    public AdminController(UserService userService, UmUtil applicationUtil, PasswordEncoder passwordEncoder,
                           LocaleConfig.LocaleSpringMessageSource adminMessageSource,
                           LocaleConfig.LocaleSpringMessageSource registrationMessageSource,
                           LocaleConfig.LocaleSpringMessageSource changeUserPasswordMessageSource,
                           LocaleConfig.LocaleSpringMessageSource adminConfirmUserMessageSource,
                           UmLanguage language) {
        this.userService = userService;
        this.secureService = applicationUtil.getAuthService();
        this.passwordEncoder = passwordEncoder;
        pageAdminParams = adminMessageSource.getKeys();
        pageRegistrationParams = registrationMessageSource.getKeys();
        pageChangeUserPasswordParams = changeUserPasswordMessageSource.getKeys();
        this.pageAdminConfirmUserParams = adminConfirmUserMessageSource.getKeys();
        this.language = language;
    }

    @GetMapping()
    public ModelAndView admin(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView;
        String authorizedName = SecurityContextHolder.getContext().getAuthentication().getName();
        if (isAuthorizedUser(authorizedName)) {
            modelAndView = new ModelAndView(PAGE_ADMIN);
            modelAndView.addObject("userName", authorizedName);
        } else {
            modelAndView = getErrorModelAndView("errorPage",
                    MessageKeys.AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
            response.setStatus(500);
        }

        addObjectsToModelAndView(modelAndView, pageAdminParams, language,
                secureService.getEncryptMethodNameForView(), lang);

        return modelAndView;
    }

    @GetMapping(value = {"/" + PAGE_CONFIRM_USER, "/" + PAGE_CONFIRM_USER + ".html"})
    public ModelAndView adminConfirmUser(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView;
        String authorizedName = SecurityContextHolder.getContext().getAuthentication().getName();
        if (isAuthorizedUser(authorizedName)) {
            modelAndView = new ModelAndView(PAGE_CONFIRM_USER);
            modelAndView.addObject("userName", authorizedName);
        } else {
            modelAndView = getErrorModelAndView("errorPage",
                    MessageKeys.AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
            response.setStatus(500);
        }

        addObjectsToModelAndView(modelAndView, pageAdminConfirmUserParams, language,
                secureService.getEncryptMethodNameForView(), lang);

        return modelAndView;
    }

    @GetMapping(value = {"/" + PAGE_REGISTRATION, "/" + PAGE_REGISTRATION + ".html"})
    public ModelAndView registration(HttpServletRequest request, HttpServletResponse response, Model model,
                                     @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView;
        String authorizedName = SecurityContextHolder.getContext().getAuthentication().getName();
        if (isAuthorizedUser(authorizedName)) {
            modelAndView = new ModelAndView(PAGE_REGISTRATION, model.asMap());
            modelAndView.addObject("userName", authorizedName);
            modelAndView.addObject("isAdmin", true);
        } else {
            modelAndView = new ModelAndView("errorPage", model.asMap());

            modelAndView.addObject("errorMsg",
                    LocaleConfig.getMessage(PAGE_REGISTRATION,
                            MessageKeys.AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang));
        }

        addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                secureService.getEncryptMethodNameForView(), lang);
        addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);
        return modelAndView;

    }

    @PostMapping(PERFORM_REGISTER_MAPPING)
    public ModelAndView performRegister(HttpServletRequest request, HttpServletResponse response,
                                        @RequestParam final String login,
                                        @RequestParam final String username,
                                        @RequestParam final String password,
                                        @RequestParam final String confirmPassword,
                                        @RequestParam final String email,
                                        @RequestParam(required = false) final String role,
                                        @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView;
        String authorizedName = SecurityContextHolder.getContext().getAuthentication().getName();

        User newUser;
        Role registerRole;
        registerRole = Role.getRole(role);

        if (!userService.accessGrantedForRegistration(registerRole, authorizedName)) {
            modelAndView = getErrorModelAndView(PAGE_REGISTRATION,
                    MessageKeys.AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
            addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            response.setStatus(500);
            return modelAndView;
        }

        try {
            if (!password.equals(confirmPassword)) {
                modelAndView = getErrorModelAndView(PAGE_REGISTRATION,
                        MessageKeys.PASSWORDS_MUST_BE_IDENTICAL_MESSAGE.getMessageKey(), lang);
                addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                        secureService.getEncryptMethodNameForView(), lang);
                response.setStatus(400);
                return modelAndView;
            }

            if (!ValidatingHelper.isValidEmail(email)) {
                modelAndView = getErrorModelAndView(PAGE_REGISTRATION,
                        MessageKeys.EMAIL_NOT_VALID_MESSAGE.getMessageKey(), lang);
                addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                        secureService.getEncryptMethodNameForView(), lang);
                response.setStatus(400);
                return modelAndView;
            }

            try {
                userService.getUserByEmail(email);
                modelAndView = getErrorModelAndView(PAGE_REGISTRATION,
                        MessageKeys.EMAIL_EXISTS_MESSAGE.getMessageKey(), lang);
                addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                        secureService.getEncryptMethodNameForView(), lang);
                response.setStatus(400);
                return modelAndView;
            } catch (EmailNotFoundException ignored) {
            }

            newUser = userService.registerUser(login, username,
                    passwordEncoder.encode(secureService.decrypt(password,
                            secureService.getSecureKey(request.getRemoteAddr()))), email, registerRole);
            userService.confirmUser(login, "", true);
        } catch (UserExistsException e) {
            modelAndView = getErrorModelAndView(PAGE_REGISTRATION,
                    MessageKeys.USER_EXISTS_PATTERN.getMessageKey(), lang, username);
            addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            response.setStatus(400);
            return modelAndView;
        } catch (Exception e) {
            modelAndView = getErrorModelAndView(PAGE_REGISTRATION,
                    MessageKeys.CREATE_USER_ERROR_MESSAGE.getMessageKey(), lang);
            addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            response.setStatus(500);
            return modelAndView;
        }

        modelAndView = getSuccessModelAndView(PAGE_REGISTRATION,
                MessageKeys.USER_CREATED_SUCCESSFULLY_PATTERN.getMessageKey(), lang, newUser.getLogin());
        addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                secureService.getEncryptMethodNameForView(), lang);

        return modelAndView;
    }

    @GetMapping(value = {"/" + PAGE_CHANGE_USER_PASSWORD, "/" + PAGE_CHANGE_USER_PASSWORD + ".html"},
            produces = {"application/json; charset=utf-8"})
    public ModelAndView changeUserPassword(HttpServletRequest request, HttpServletResponse response,
                                           @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView = new ModelAndView(PAGE_CHANGE_USER_PASSWORD);
        String authorizedName = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!isAuthorizedUser(authorizedName)) {
            modelAndView = getErrorModelAndView("errorPage",
                    MessageKeys.AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
        }

        addObjectsToModelAndView(modelAndView, pageChangeUserPasswordParams, language,
                secureService.getEncryptMethodNameForView(), lang);
        addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);
        return modelAndView;
    }

    @PostMapping(PERFORM_CHANGE_USER_PASSWORD)
    public ModelAndView performChangeUserPassword(HttpServletRequest request, HttpServletResponse response,
                                                  @RequestParam String userName, @RequestParam String newPassword,
                                                  @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView;
        try {
            String authorizedUserName = SecurityContextHolder.getContext().getAuthentication().getName();
            if (isAuthorizedUser(authorizedUserName)) {
                if (authorizedUserName.toLowerCase().contains("anonymoususer")) {
                    modelAndView = getErrorModelAndView(PAGE_CHANGE_USER_PASSWORD,
                            MessageKeys.AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
                    addObjectsToModelAndView(modelAndView, pageChangeUserPasswordParams, language,
                            secureService.getEncryptMethodNameForView(), lang);
                    response.setStatus(403);
                    return modelAndView;
                }
                User user = userService.getUserByName(userName);
                String realNewPassword = secureService.decrypt(newPassword,
                        secureService.getSecureKey(request.getRemoteAddr()));
                if (user != null) {
                    user.setPassword(passwordEncoder.encode(realNewPassword));
                    userService.updateUser(user);
                } else {
                    modelAndView = getErrorModelAndView(PAGE_CHANGE_USER_PASSWORD,
                            MessageKeys.USER_NOT_FOUND_MESSAGE.getMessageKey(), lang);
                    addObjectsToModelAndView(modelAndView, pageChangeUserPasswordParams, language,
                            secureService.getEncryptMethodNameForView(), lang);
                    response.setStatus(404);
                    return modelAndView;
                }
            } else {
                modelAndView = getErrorModelAndView(PAGE_CHANGE_USER_PASSWORD,
                        MessageKeys.REQUEST_PROCESSING_ERROR.getMessageKey(), lang);
                addObjectsToModelAndView(modelAndView, pageChangeUserPasswordParams, language,
                        secureService.getEncryptMethodNameForView(), lang);
                response.setStatus(500);
                return modelAndView;
            }

        } catch (UsernameNotFoundException ex) {
            modelAndView = getErrorModelAndView(PAGE_CHANGE_USER_PASSWORD,
                    MessageKeys.USER_NOT_FOUND_MESSAGE.getMessageKey(), lang);
            addObjectsToModelAndView(modelAndView, pageChangeUserPasswordParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            response.setStatus(404);
            return modelAndView;
        } catch (Exception ex) {
            modelAndView = getErrorModelAndView(PAGE_CHANGE_USER_PASSWORD,
                    MessageKeys.REQUEST_PROCESSING_ERROR.getMessageKey(), lang);
            addObjectsToModelAndView(modelAndView, pageChangeUserPasswordParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            response.setStatus(500);
            return modelAndView;
        }

        modelAndView = getSuccessModelAndView(PAGE_CHANGE_USER_PASSWORD,
                MessageKeys.PASSWORD_SUCCESSFULLY_CHANGED_MESSAGE.getMessageKey(), lang);
        response.setStatus(200);
        addObjectsToModelAndView(modelAndView, pageChangeUserPasswordParams, language,
                secureService.getEncryptMethodNameForView(), lang);
        addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);

        return modelAndView;
    }

    private boolean isAuthorizedUser(String userName) {
        return (userName != null && !userName.isEmpty());
    }
}

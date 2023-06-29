package com.github.vssavin.umlib.user;

import com.github.vssavin.umlib.config.LocaleConfig;
import com.github.vssavin.umlib.config.UmConfig;
import com.github.vssavin.umlib.email.EmailNotFoundException;
import com.github.vssavin.umlib.helper.MvcHelper;
import com.github.vssavin.umlib.helper.ValidatingHelper;
import com.github.vssavin.umlib.language.MessageKeys;
import com.github.vssavin.umlib.language.UmLanguage;
import com.github.vssavin.umlib.security.SecureService;
import io.github.vssavin.securelib.Utils;
import com.github.vssavin.umlib.data.pagination.Paged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Set;

import static com.github.vssavin.umlib.user.AdminController.ADMIN_CONTROLLER_PATH;
import static com.github.vssavin.umlib.helper.MvcHelper.*;

/**
 * @author vssavin on 21.12.21
 */
@RestController
@RequestMapping(ADMIN_CONTROLLER_PATH)
class AdminController {
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    static final String ADMIN_CONTROLLER_PATH = "/um/admin";
    private static final String PAGE_LOGIN = UmConfig.LOGIN_URL.replaceAll("/", "");
    private static final String PAGE_USERS = "users";
    private static final String PAGE_USER_EDIT = "edit";
    private static final String PAGE_EDIT = "userEdit";
    private static final String PAGE_ADMIN = "admin";
    private static final String PAGE_REGISTRATION = "registration";
    private static final String PAGE_CHANGE_USER_PASSWORD = "changeUserPassword";
    private static final String PAGE_CONFIRM_USER = "adminConfirmUser";

    private static final String PERFORM_REGISTER_MAPPING = "/perform-register";

    private static final Set<String> IGNORED_PARAMS = new HashSet<>();

    static {
        IGNORED_PARAMS.add("_csrf");
        IGNORED_PARAMS.add("newPassword");
    }

    private final Set<String> pageUsersParams;
    private final Set<String> pageUserEditParams;
    private final Set<String> pageLoginParams;
    private final Set<String> pageAdminParams;
    private final Set<String> pageRegistrationParams;
    private final Set<String> pageChangeUserPasswordParams;
    private final Set<String> pageAdminConfirmUserParams;

    private final UserService userService;
    private final SecureService secureService;
    private final PasswordEncoder passwordEncoder;
    private final UmLanguage language;

    @Autowired
    AdminController(LocaleConfig localeConfig, UserService userService, UmConfig umConfig,
                    PasswordEncoder passwordEncoder, UmLanguage language) {
        this.userService = userService;
        this.secureService = umConfig.getAuthService();
        this.passwordEncoder = passwordEncoder;
        pageLoginParams = localeConfig.forPage(PAGE_LOGIN).getKeys();
        pageUsersParams = localeConfig.forPage(PAGE_USERS).getKeys();
        pageUserEditParams = localeConfig.forPage(PAGE_EDIT).getKeys();
        pageAdminParams = localeConfig.forPage(PAGE_ADMIN).getKeys();
        pageRegistrationParams = localeConfig.forPage(PAGE_REGISTRATION).getKeys();
        pageChangeUserPasswordParams = localeConfig.forPage(PAGE_CHANGE_USER_PASSWORD).getKeys();
        this.pageAdminConfirmUserParams = localeConfig.forPage(PAGE_CONFIRM_USER).getKeys();
        this.language = language;
    }

    @GetMapping()
    ModelAndView admin(HttpServletResponse response, @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView;
        String authorizedName = UserSecurityHelper.getAuthorizedUserName(userService);
        if (isAuthorizedUser(authorizedName)) {
            modelAndView = new ModelAndView(PAGE_ADMIN);
            modelAndView.addObject("userName", authorizedName);
        } else {
            modelAndView = MvcHelper.getErrorModelAndView("errorPage",
                    MessageKeys.AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
            response.setStatus(500);
        }

        MvcHelper.addObjectsToModelAndView(modelAndView, pageAdminParams, language,
                secureService.getEncryptMethodNameForView(), lang);

        return modelAndView;
    }

    @GetMapping(value = {"/" + PAGE_CONFIRM_USER, "/" + PAGE_CONFIRM_USER + ".html"})
    ModelAndView adminConfirmUser(HttpServletResponse response,
                                         @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView;
        String authorizedName = UserSecurityHelper.getAuthorizedUserName(userService);
        if (isAuthorizedUser(authorizedName)) {
            modelAndView = new ModelAndView(PAGE_CONFIRM_USER);
            modelAndView.addObject("userName", authorizedName);
        } else {
            modelAndView = MvcHelper.getErrorModelAndView("errorPage",
                    MessageKeys.AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
            response.setStatus(500);
        }

        MvcHelper.addObjectsToModelAndView(modelAndView, pageAdminConfirmUserParams, language,
                secureService.getEncryptMethodNameForView(), lang);

        return modelAndView;
    }

    @GetMapping(value = {"/" + PAGE_REGISTRATION, "/" + PAGE_REGISTRATION + ".html"})
    ModelAndView registration(HttpServletRequest request, Model model,
                                     @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView;
        String authorizedName = UserSecurityHelper.getAuthorizedUserName(userService);
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

        MvcHelper.addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                secureService.getEncryptMethodNameForView(), lang);
        MvcHelper.addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);
        return modelAndView;

    }

    @PostMapping(PERFORM_REGISTER_MAPPING)
    ModelAndView performRegister(HttpServletRequest request, HttpServletResponse response,
                                        @RequestParam final String login,
                                        @RequestParam final String username,
                                        @RequestParam final String password,
                                        @RequestParam final String confirmPassword,
                                        @RequestParam final String email,
                                        @RequestParam(required = false) final String role,
                                        @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView;
        String authorizedName = UserSecurityHelper.getAuthorizedUserLogin();

        User newUser;
        Role registerRole;
        registerRole = Role.getRole(role);

        if (!userService.accessGrantedForRegistration(registerRole, authorizedName)) {
            modelAndView = MvcHelper.getErrorModelAndView(PAGE_REGISTRATION,
                    MessageKeys.AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
            MvcHelper.addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            response.setStatus(500);
            return modelAndView;
        }

        try {
            String key = secureService.getSecureKey(request.getRemoteAddr());
            String decodedPassword = secureService.decrypt(password, key);
            String decodedConfirmPassword = secureService.decrypt(confirmPassword, key);
            if (!decodedPassword.equals(decodedConfirmPassword)) {
                modelAndView = MvcHelper.getErrorModelAndView(PAGE_REGISTRATION,
                        MessageKeys.PASSWORDS_MUST_BE_IDENTICAL_MESSAGE.getMessageKey(), lang);
                MvcHelper.addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                        secureService.getEncryptMethodNameForView(), lang);
                response.setStatus(400);
                return modelAndView;
            }

            if (!ValidatingHelper.isValidEmail(email)) {
                modelAndView = MvcHelper.getErrorModelAndView(PAGE_REGISTRATION,
                        MessageKeys.EMAIL_NOT_VALID_MESSAGE.getMessageKey(), lang);
                MvcHelper.addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                        secureService.getEncryptMethodNameForView(), lang);
                response.setStatus(400);
                return modelAndView;
            }

            try {
                userService.getUserByEmail(email);
                modelAndView = MvcHelper.getErrorModelAndView(PAGE_REGISTRATION,
                        MessageKeys.EMAIL_EXISTS_MESSAGE.getMessageKey(), lang);
                MvcHelper.addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                        secureService.getEncryptMethodNameForView(), lang);
                response.setStatus(400);
                return modelAndView;
            } catch (EmailNotFoundException ignored) {
            }

            newUser = userService.registerUser(login, username,
                    passwordEncoder.encode(decodedPassword), email, registerRole);
            Utils.clearString(decodedPassword);
            Utils.clearString(decodedConfirmPassword);
            userService.confirmUser(login, "", true);
        } catch (UserExistsException e) {
            log.error("User exists! ", e);
            modelAndView = MvcHelper.getErrorModelAndView(PAGE_REGISTRATION,
                    MessageKeys.USER_EXISTS_PATTERN.getMessageKey(), lang, username);
            MvcHelper.addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            response.setStatus(400);
            return modelAndView;
        } catch (Exception e) {
            log.error("User registration error! ", e);
            modelAndView = MvcHelper.getErrorModelAndView(PAGE_REGISTRATION,
                    MessageKeys.CREATE_USER_ERROR_MESSAGE.getMessageKey(), lang);
            MvcHelper.addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            response.setStatus(500);
            return modelAndView;
        }

        modelAndView = getSuccessModelAndView(PAGE_REGISTRATION,
                MessageKeys.USER_CREATED_SUCCESSFULLY_PATTERN.getMessageKey(), lang, newUser.getLogin());
        MvcHelper.addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                secureService.getEncryptMethodNameForView(), lang);

        return modelAndView;
    }

    @GetMapping(value = {"/" + PAGE_CHANGE_USER_PASSWORD, "/" + PAGE_CHANGE_USER_PASSWORD + ".html"},
            produces = {"application/json; charset=utf-8"})
    ModelAndView changeUserPassword(HttpServletRequest request,
                                           @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView = new ModelAndView(PAGE_CHANGE_USER_PASSWORD);
        String authorizedName = UserSecurityHelper.getAuthorizedUserName(userService);
        if (!isAuthorizedUser(authorizedName)) {
            modelAndView = MvcHelper.getErrorModelAndView("errorPage",
                    MessageKeys.AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
        }

        MvcHelper.addObjectsToModelAndView(modelAndView, pageChangeUserPasswordParams, language,
                secureService.getEncryptMethodNameForView(), lang);
        MvcHelper.addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);
        return modelAndView;
    }

    @PatchMapping(PAGE_CHANGE_USER_PASSWORD)
    ModelAndView performChangeUserPassword(HttpServletRequest request, HttpServletResponse response,
                                                  @RequestParam String userName, @RequestParam String newPassword,
                                                  @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView;
        try {
            String authorizedUserName = UserSecurityHelper.getAuthorizedUserName(userService);
            if (isAuthorizedUser(authorizedUserName)) {
                User user = userService.getUserByLogin(userName);
                String realNewPassword = secureService.decrypt(newPassword,
                        secureService.getSecureKey(request.getRemoteAddr()));
                if (user != null) {
                    user.setPassword(passwordEncoder.encode(realNewPassword));
                    Utils.clearString(realNewPassword);
                    userService.updateUser(user);
                } else {
                    modelAndView = MvcHelper.getErrorModelAndView(PAGE_CHANGE_USER_PASSWORD,
                            MessageKeys.USER_NOT_FOUND_MESSAGE.getMessageKey(), lang);
                    MvcHelper.addObjectsToModelAndView(modelAndView, pageChangeUserPasswordParams, language,
                            secureService.getEncryptMethodNameForView(), lang);
                    response.setStatus(404);
                    return modelAndView;
                }
            } else {
                modelAndView = MvcHelper.getErrorModelAndView(PAGE_CHANGE_USER_PASSWORD,
                        MessageKeys.AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
                MvcHelper.addObjectsToModelAndView(modelAndView, pageChangeUserPasswordParams, language,
                        secureService.getEncryptMethodNameForView(), lang);
                response.setStatus(403);
                return modelAndView;
            }

        } catch (UsernameNotFoundException ex) {
            log.error("User name not found!", ex);
            modelAndView = MvcHelper.getErrorModelAndView(PAGE_CHANGE_USER_PASSWORD,
                    MessageKeys.USER_NOT_FOUND_MESSAGE.getMessageKey(), lang);
            MvcHelper.addObjectsToModelAndView(modelAndView, pageChangeUserPasswordParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            response.setStatus(404);
            return modelAndView;
        } catch (Exception ex) {
            log.error("User password change error! ", ex);
            modelAndView = MvcHelper.getErrorModelAndView(PAGE_CHANGE_USER_PASSWORD,
                    MessageKeys.REQUEST_PROCESSING_ERROR.getMessageKey(), lang);
            MvcHelper.addObjectsToModelAndView(modelAndView, pageChangeUserPasswordParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            response.setStatus(500);
            return modelAndView;
        }

        modelAndView = MvcHelper.getSuccessModelAndView(PAGE_CHANGE_USER_PASSWORD,
                MessageKeys.PASSWORD_SUCCESSFULLY_CHANGED_MESSAGE.getMessageKey(), lang);
        response.setStatus(200);
        MvcHelper.addObjectsToModelAndView(modelAndView, pageChangeUserPasswordParams, language,
                secureService.getEncryptMethodNameForView(), lang);
        MvcHelper.addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);

        return modelAndView;
    }

    @GetMapping(value = {"/" + PAGE_USERS, "/" + PAGE_USERS + ".html"})
    ModelAndView users(HttpServletRequest request, HttpServletResponse response,
                              @ModelAttribute UserFilter userFilter,
                              @RequestParam(required = false, defaultValue = "1") final int page,
                              @RequestParam(required = false, defaultValue = "5") final int size,
                              @RequestParam(required = false) final String lang) {

        ModelAndView modelAndView = new ModelAndView(PAGE_USERS);
        if (UserSecurityHelper.isAuthorizedAdmin(userService)) {
            Paged<User> users = userService.getUsers(userFilter, page, size);
            modelAndView.addObject("users", users);
        } else {
            modelAndView = MvcHelper.getErrorModelAndView(UmConfig.LOGIN_URL,
                    MessageKeys.ADMIN_AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
            MvcHelper.addObjectsToModelAndView(modelAndView, pageLoginParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            response.setStatus(403);
        }

        MvcHelper.addObjectsToModelAndView(modelAndView, pageUsersParams, language,
                secureService.getEncryptMethodNameForView(), lang);
        MvcHelper.addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);

        return modelAndView;
    }

    @GetMapping(value = {"/" + PAGE_USERS + "/" + PAGE_USER_EDIT + "/{id}",
            "/" + PAGE_USERS + "/" + PAGE_USER_EDIT + ".html" + "/{id}"})
    ModelAndView userEdit(HttpServletRequest request, HttpServletResponse response,
                                 @PathVariable Long id,
                                 @RequestParam(required = false) final boolean success,
                                 @RequestParam(required = false) final String successMsg,
                                 @RequestParam(required = false) final boolean error,
                                 @RequestParam(required = false) final String errorMsg,
                                 @RequestParam(required = false) final String lang) {

        ModelAndView modelAndView = new ModelAndView("userEdit");
        if (UserSecurityHelper.isAuthorizedAdmin(userService)) {
            User user = userService.getUserById(id);
            modelAndView.addObject("user", user);
        } else {
            modelAndView = MvcHelper.getErrorModelAndView(UmConfig.LOGIN_URL,
                    MessageKeys.ADMIN_AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
            MvcHelper.addObjectsToModelAndView(modelAndView, pageLoginParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            response.setStatus(403);
            MvcHelper.addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);
            return modelAndView;
        }

        MvcHelper.addObjectsToModelAndView(modelAndView, pageUserEditParams, language,
                secureService.getEncryptMethodNameForView(), lang);
        MvcHelper.addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);

        if (successMsg != null) {
            modelAndView.addObject("success", success);
            modelAndView.addObject("successMsg", successMsg);
        }

        if (errorMsg != null) {
            modelAndView.addObject("error", error);
            modelAndView.addObject("errorMsg", errorMsg);
        }

        return modelAndView;
    }

    @PatchMapping(PAGE_USERS)
    ModelAndView performUserEdit(HttpServletRequest request, HttpServletResponse response,
                                        @ModelAttribute UserDto userDto,
                                        @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView = new ModelAndView("userEdit");
        try {
            if (UserSecurityHelper.isAuthorizedAdmin(userService)) {
                if (!ValidatingHelper.isValidEmail(userDto.getEmail())) {
                    modelAndView = MvcHelper.getErrorModelAndView(PAGE_USERS,
                            MessageKeys.EMAIL_NOT_VALID_MESSAGE.getMessageKey(), lang);
                    MvcHelper.addObjectsToModelAndView(modelAndView, pageUsersParams, language,
                            secureService.getEncryptMethodNameForView(), lang);
                    response.setStatus(400);
                    return modelAndView;
                }

                User userByLogin = null;
                try {
                    userByLogin = userService.getUserByLogin(userDto.getLogin());
                } catch (UsernameNotFoundException e) {
                    //ignore
                }

                if (userByLogin != null && !userByLogin.getId().equals(userDto.getId())) {
                    modelAndView = MvcHelper.getErrorModelAndView(PAGE_USERS,
                            MessageKeys.USER_EXISTS_PATTERN.getMessageKey(), lang, userDto.getLogin());
                    MvcHelper.addObjectsToModelAndView(modelAndView, pageUsersParams, language,
                            secureService.getEncryptMethodNameForView(), lang);
                    response.setStatus(400);
                    return modelAndView;
                }

                User userFromDatabase = userService.getUserById(userDto.getId());
                User newUser = User.builder().id(userFromDatabase.getId()).login(userDto.getLogin())
                        .name(userDto.getName()).password(userFromDatabase.getPassword())
                        .email(userDto.getEmail()).authority(userFromDatabase.getAuthority())
                        .expirationDate(userFromDatabase.getExpirationDate())
                        .verificationId(userFromDatabase.getVerificationId())
                        .build();
                newUser = userService.updateUser(newUser);
                modelAndView.addObject("user", newUser);
                modelAndView.addObject("success", true);
                String successMsg = LocaleConfig
                        .getMessage("userEdit", MessageKeys.USER_EDIT_SUCCESS_MESSAGE.getMessageKey(), lang);
                modelAndView.addObject("successMsg", successMsg);
            } else {
                modelAndView = MvcHelper.getErrorModelAndView(UmConfig.LOGIN_URL,
                        MessageKeys.ADMIN_AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
                MvcHelper.addObjectsToModelAndView(modelAndView, pageLoginParams, language,
                        secureService.getEncryptMethodNameForView(), lang);
                response.setStatus(403);
                return modelAndView;
            }
        } catch (Exception e) {
            log.error("User update error! ", e);
            modelAndView = MvcHelper.getErrorModelAndView(UmConfig.LOGIN_URL,
                    MessageKeys.USER_EDIT_ERROR_MESSAGE.getMessageKey(), lang);
            MvcHelper.addObjectsToModelAndView(modelAndView, pageUserEditParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            return modelAndView;
        }

        modelAndView = new ModelAndView("redirect:" + ADMIN_CONTROLLER_PATH + "/" + PAGE_USERS +
                "/" + PAGE_USER_EDIT + "/" + userDto.getId());

        MvcHelper.addObjectsToModelAndView(modelAndView, "userEdit", pageUserEditParams, language,
                secureService.getEncryptMethodNameForView(), lang);
        MvcHelper.addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);

        String successMsg = LocaleConfig.getMessage("userEdit",
                MessageKeys.USER_EDIT_SUCCESS_MESSAGE.getMessageKey(), lang);
        modelAndView.addObject("success", true);
        modelAndView.addObject("successMsg", successMsg);
        return modelAndView;
    }

    @DeleteMapping(PAGE_USERS)
    ModelAndView deleteUser(HttpServletRequest request,
                                   HttpServletResponse response,
                                   @RequestParam Long id,
                                   @RequestParam(required = false, defaultValue = "1") final int page,
                                   @RequestParam(required = false, defaultValue = "5") final int size,
                                   @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView = new ModelAndView("users");
        if (UserSecurityHelper.isAuthorizedAdmin(userService)) {
            User user = userService.getUserById(id);
            if (user.getLogin().isEmpty()) {
                String errorMessage = LocaleConfig.getMessage("users",
                        MessageKeys.USER_DELETE_ERROR_MESSAGE.getMessageKey(), lang);
                modelAndView.addObject("error", true);
                modelAndView.addObject("errorMsg", errorMessage);
            }
            else  {
                userService.deleteUser(user);
            }

        } else {
            modelAndView = MvcHelper.getErrorModelAndView(UmConfig.LOGIN_URL,
                    MessageKeys.ADMIN_AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
            MvcHelper.addObjectsToModelAndView(modelAndView, pageLoginParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            response.setStatus(403);
            return modelAndView;
        }

        Paged<User> users = userService.getUsers(UserFilter.emptyUserFilter(), page, size);
        modelAndView.addObject("users", users);

        MvcHelper.addObjectsToModelAndView(modelAndView, pageUsersParams, language,
                secureService.getEncryptMethodNameForView(), lang);
        MvcHelper.addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);

        return modelAndView;
    }

    private boolean isAuthorizedUser(String userName) {
        return (userName != null && !userName.isEmpty());
    }
}

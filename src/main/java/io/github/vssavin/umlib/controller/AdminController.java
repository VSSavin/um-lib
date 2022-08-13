package io.github.vssavin.umlib.controller;

import io.github.vssavin.umlib.config.LocaleConfig;
import io.github.vssavin.umlib.config.UmConfig;
import io.github.vssavin.umlib.dto.UserDto;
import io.github.vssavin.umlib.dto.UserFilter;
import io.github.vssavin.umlib.entity.Role;
import io.github.vssavin.umlib.entity.User;
import io.github.vssavin.umlib.exception.EmailNotFoundException;
import io.github.vssavin.umlib.exception.UserExistsException;
import io.github.vssavin.umlib.helper.SecurityHelper;
import io.github.vssavin.umlib.helper.ValidatingHelper;
import io.github.vssavin.umlib.language.UmLanguage;
import io.github.vssavin.umlib.pagination.Paged;
import io.github.vssavin.umlib.service.SecureService;
import io.github.vssavin.umlib.service.UserService;
import io.github.vssavin.umlib.utils.UmUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private static final String PAGE_USERS = "users";
    private static final String PAGE_USER_EDIT = "edit";
    private static final String PAGE_ADMIN = "admin";
    private static final String PAGE_REGISTRATION = "registration";
    private static final String PAGE_CHANGE_USER_PASSWORD = "changeUserPassword";
    private static final String PAGE_CONFIRM_USER = "adminConfirmUser";

    private static final String PERFORM_REGISTER_MAPPING = "/perform-register";
    private static final String PERFORM_CHANGE_USER_PASSWORD = "/perform-change-user-password";
    private static final String PERFORM_USER_EDIT = "/users/edit/perform-user-edit";
    private static final String PERFORM_USER_DELETE = "/users/perform-delete";

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

    public AdminController(UserService userService, UmUtil applicationUtil, PasswordEncoder passwordEncoder,
                           LocaleConfig.LocaleSpringMessageSource loginMessageSource,
                           LocaleConfig.LocaleSpringMessageSource usersMessageSource,
                           LocaleConfig.LocaleSpringMessageSource userEditMessageSource,
                           LocaleConfig.LocaleSpringMessageSource adminMessageSource,
                           LocaleConfig.LocaleSpringMessageSource registrationMessageSource,
                           LocaleConfig.LocaleSpringMessageSource changeUserPasswordMessageSource,
                           LocaleConfig.LocaleSpringMessageSource adminConfirmUserMessageSource,
                           UmLanguage language) {
        this.userService = userService;
        this.secureService = applicationUtil.getAuthService();
        this.passwordEncoder = passwordEncoder;
        pageLoginParams = loginMessageSource.getKeys();
        pageUsersParams = usersMessageSource.getKeys();
        pageUserEditParams = userEditMessageSource.getKeys();
        pageAdminParams = adminMessageSource.getKeys();
        pageRegistrationParams = registrationMessageSource.getKeys();
        pageChangeUserPasswordParams = changeUserPasswordMessageSource.getKeys();
        this.pageAdminConfirmUserParams = adminConfirmUserMessageSource.getKeys();
        this.language = language;
    }

    @GetMapping()
    public ModelAndView admin(HttpServletResponse response, @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView;
        String authorizedName = SecurityHelper.getAuthorizedUserName(userService);
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
    public ModelAndView adminConfirmUser(HttpServletResponse response,
                                         @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView;
        String authorizedName = SecurityHelper.getAuthorizedUserName(userService);
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
    public ModelAndView registration(HttpServletRequest request, Model model,
                                     @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView;
        String authorizedName = SecurityHelper.getAuthorizedUserName(userService);
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
        String authorizedName = SecurityHelper.getAuthorizedUserLogin();

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
            log.error("User exists! ", e);
            modelAndView = getErrorModelAndView(PAGE_REGISTRATION,
                    MessageKeys.USER_EXISTS_PATTERN.getMessageKey(), lang, username);
            addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            response.setStatus(400);
            return modelAndView;
        } catch (Exception e) {
            log.error("User registration error! ", e);
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
    public ModelAndView changeUserPassword(HttpServletRequest request,
                                           @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView = new ModelAndView(PAGE_CHANGE_USER_PASSWORD);
        String authorizedName = SecurityHelper.getAuthorizedUserName(userService);
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
            String authorizedUserName = SecurityHelper.getAuthorizedUserName(userService);
            if (isAuthorizedUser(authorizedUserName)) {
                User user = userService.getUserByLogin(userName);
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
                        MessageKeys.AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
                addObjectsToModelAndView(modelAndView, pageChangeUserPasswordParams, language,
                        secureService.getEncryptMethodNameForView(), lang);
                response.setStatus(403);
                return modelAndView;
            }

        } catch (UsernameNotFoundException ex) {
            log.error("User name not found!", ex);
            modelAndView = getErrorModelAndView(PAGE_CHANGE_USER_PASSWORD,
                    MessageKeys.USER_NOT_FOUND_MESSAGE.getMessageKey(), lang);
            addObjectsToModelAndView(modelAndView, pageChangeUserPasswordParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            response.setStatus(404);
            return modelAndView;
        } catch (Exception ex) {
            log.error("User password change error! ", ex);
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

    @GetMapping(value = {"/" + PAGE_USERS, "/" + PAGE_USERS + ".html"})
    public ModelAndView users(HttpServletRequest request, HttpServletResponse response,
                              @ModelAttribute UserFilter userFilter,
                              @RequestParam(required = false, defaultValue = "1") final int page,
                              @RequestParam(required = false, defaultValue = "5") final int size,
                              @RequestParam(required = false) final String lang) {

        ModelAndView modelAndView = new ModelAndView("users");
        if (SecurityHelper.isAuthorizedAdmin(userService)) {
            Paged<User> users = userService.getUsers(userFilter, page, size);
            modelAndView.addObject("users", users);
        } else {
            modelAndView = getErrorModelAndView(UmConfig.LOGIN_URL,
                    MessageKeys.ADMIN_AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
            addObjectsToModelAndView(modelAndView, pageLoginParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            response.setStatus(403);
        }

        addObjectsToModelAndView(modelAndView, pageUsersParams, language,
                secureService.getEncryptMethodNameForView(), lang);
        addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);

        return modelAndView;
    }

    @GetMapping(value = {"/" + PAGE_USERS + "/" + PAGE_USER_EDIT + "/{id}",
            "/" + PAGE_USERS + "/" + PAGE_USER_EDIT + ".html" + "/{id}"})
    public ModelAndView userEdit(HttpServletRequest request, HttpServletResponse response,
                                 @PathVariable Long id,
                                 @RequestParam(required = false) final boolean success,
                                 @RequestParam(required = false) final String successMsg,
                                 @RequestParam(required = false) final boolean error,
                                 @RequestParam(required = false) final String errorMsg,
                                 @RequestParam(required = false) final String lang) {

        ModelAndView modelAndView = new ModelAndView("userEdit");
        if (SecurityHelper.isAuthorizedAdmin(userService)) {
            User user = userService.getUserById(id);
            modelAndView.addObject("user", user);
        } else {
            modelAndView = getErrorModelAndView(UmConfig.LOGIN_URL,
                    MessageKeys.ADMIN_AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
            addObjectsToModelAndView(modelAndView, pageLoginParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            response.setStatus(403);
            addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);
            return modelAndView;
        }

        addObjectsToModelAndView(modelAndView, pageUserEditParams, language,
                secureService.getEncryptMethodNameForView(), lang);
        addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);

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

    @PostMapping(PERFORM_USER_EDIT)
    public ModelAndView performUserEdit(HttpServletRequest request, HttpServletResponse response,
                                        @ModelAttribute UserDto userDto,
                                        @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView = new ModelAndView("userEdit");
        try {
            if (SecurityHelper.isAuthorizedAdmin(userService)) {
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
                modelAndView = getErrorModelAndView(UmConfig.LOGIN_URL,
                        MessageKeys.ADMIN_AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
                addObjectsToModelAndView(modelAndView, pageLoginParams, language,
                        secureService.getEncryptMethodNameForView(), lang);
                response.setStatus(403);
                return modelAndView;
            }
        } catch (Exception e) {
            log.error("User update error! ", e);
            modelAndView = getErrorModelAndView(UmConfig.LOGIN_URL,
                    MessageKeys.USER_EDIT_ERROR_MESSAGE.getMessageKey(), lang);
            addObjectsToModelAndView(modelAndView, pageUserEditParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            return modelAndView;
        }

        modelAndView = new ModelAndView("redirect:/" + PAGE_ADMIN + "/" + PAGE_USERS +
                "/" + PAGE_USER_EDIT + "/" + userDto.getId());

        addObjectsToModelAndView(modelAndView, "userEdit", pageUserEditParams, language,
                secureService.getEncryptMethodNameForView(), lang);
        addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);

        String successMsg = LocaleConfig.getMessage("userEdit",
                MessageKeys.USER_EDIT_SUCCESS_MESSAGE.getMessageKey(), lang);
        modelAndView.addObject("success", true);
        modelAndView.addObject("successMsg", successMsg);
        return modelAndView;
    }

    @DeleteMapping(PAGE_USERS)
    public ModelAndView deleteUser(HttpServletRequest request,
                                   HttpServletResponse response,
                                   @RequestParam Long id,
                                   @RequestParam(required = false, defaultValue = "1") final int page,
                                   @RequestParam(required = false, defaultValue = "5") final int size,
                                   @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView = new ModelAndView("users");
        if (SecurityHelper.isAuthorizedAdmin(userService)) {
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
            modelAndView = getErrorModelAndView(UmConfig.LOGIN_URL,
                    MessageKeys.ADMIN_AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
            addObjectsToModelAndView(modelAndView, pageLoginParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            response.setStatus(403);
            return modelAndView;
        }

        Paged<User> users = userService.getUsers(UserFilter.emptyUserFilter(), page, size);
        modelAndView.addObject("users", users);

        addObjectsToModelAndView(modelAndView, pageUsersParams, language,
                secureService.getEncryptMethodNameForView(), lang);
        addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);

        return modelAndView;
    }

    private boolean isAuthorizedUser(String userName) {
        return (userName != null && !userName.isEmpty());
    }
}

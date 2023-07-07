package com.github.vssavin.umlib.user;

import com.github.vssavin.umlib.config.LocaleConfig;
import com.github.vssavin.umlib.config.UmConfig;
import com.github.vssavin.umlib.email.EmailNotFoundException;
import com.github.vssavin.umlib.email.EmailService;
import com.github.vssavin.umlib.helper.MvcHelper;
import com.github.vssavin.umlib.helper.ValidationHelper;
import com.github.vssavin.umlib.language.MessageKeys;
import com.github.vssavin.umlib.language.UmLanguage;
import com.github.vssavin.umlib.security.SecureService;
import io.github.vssavin.securelib.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static com.github.vssavin.umlib.helper.MvcHelper.*;

/**
 * @author vssavin on 23.12.21
 */
@RestController
@RequestMapping(UserController.USER_CONTROLLER_PATH)
class UserController {
    static final String USER_CONTROLLER_PATH = "/um/users";
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private static final String PAGE_LOGIN = UmConfig.LOGIN_URL.replaceAll("/", "");
    private static final String PAGE_REGISTRATION = "registration";
    private static final String PAGE_CHANGE_PASSWORD = "changePassword";
    private static final String PAGE_CONFIRM_USER = "confirmUser";
    private static final String PAGE_USER_EDIT = "userEditYourself";
    private static final String PAGE_USER_CONTROL_PANEL = "userControlPanel";
    private static final String PERFORM_REGISTER_MAPPING = "/perform-register";
    private static final String PAGE_RECOVERY_PASSWORD = "passwordRecovery";
    private static final String PERFORM_PASSWORD_RECOVERY = "/perform-password-recovery";

    private static final Set<String> IGNORED_PARAMS = new HashSet<>();

    static {
        IGNORED_PARAMS.add("_csrf");
        IGNORED_PARAMS.add("newPassword");
        IGNORED_PARAMS.add("currentPassword");
    }

    private final Set<String> pageRegistrationParams;
    private final Set<String> pageChangePasswordParams;
    private final Set<String> pageConfirmUserParams;
    private final Set<String> pagePasswordRecoveryParams;
    private final Set<String> pageLoginParams;
    private final Set<String> pageUserEditParams;
    private final Set<String> pageUserControlPanelParams;

    private final UserService userService;
    private final SecureService secureService;
    private final EmailService emailService;
    private final UmConfig mainConfig;
    private final PasswordEncoder passwordEncoder;
    private final UmLanguage language;

    @Autowired
    UserController(LocaleConfig localeConfig, UserService userService, EmailService emailService,
                   UmConfig umConfig, PasswordEncoder passwordEncoder, UmLanguage language) {
        this.userService = userService;
        this.secureService = umConfig.getAuthService();
        this.emailService = emailService;
        this.mainConfig = umConfig;
        this.pageUserEditParams = localeConfig.forPage(PAGE_USER_EDIT).getKeys();
        this.pageUserControlPanelParams = localeConfig.forPage(PAGE_USER_CONTROL_PANEL).getKeys();
        this.pageLoginParams = localeConfig.forPage(PAGE_LOGIN).getKeys();
        this.pageRegistrationParams = localeConfig.forPage(PAGE_REGISTRATION).getKeys();
        this.pageChangePasswordParams = localeConfig.forPage(PAGE_CHANGE_PASSWORD).getKeys();
        this.pageConfirmUserParams = localeConfig.forPage(PAGE_CONFIRM_USER).getKeys();
        this.pagePasswordRecoveryParams = localeConfig.forPage(PAGE_RECOVERY_PASSWORD).getKeys();
        this.passwordEncoder = passwordEncoder;
        this.language = language;
    }

    @GetMapping(value = {"/" + PAGE_REGISTRATION, "/" + PAGE_REGISTRATION + ".html"})
    ModelAndView registration(HttpServletRequest request, Model model,
                                     @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView;

        if (!mainConfig.getRegistrationAllowed()) {
            return getForbiddenModelAndView(request);
        }

        String authorizedName = UserSecurityHelper.getAuthorizedUserName(userService);
        if (!authorizedName.isEmpty()) {
            return getForbiddenModelAndView(request);
        }

        modelAndView = new ModelAndView(PAGE_REGISTRATION, model.asMap());
        modelAndView.addObject("userName", authorizedName);

        MvcHelper.addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                secureService.getEncryptMethodNameForView(), lang);
        MvcHelper.addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);
        return modelAndView;
    }

    @PostMapping(PERFORM_REGISTER_MAPPING)
    ModelAndView performRegister(HttpServletRequest request, HttpServletResponse response,
                                        @RequestParam final String login,
                                        @RequestParam final String username,
                                        @RequestParam final String email,
                                        @RequestParam final String password,
                                        @RequestParam final String confirmPassword,
                                        @RequestParam(required = false) final String role,
                                        @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView;
        if (!mainConfig.getRegistrationAllowed()) {
            return getForbiddenModelAndView(request);
        }

        String authorizedName = UserSecurityHelper.getAuthorizedUserName(userService);
        if (!authorizedName.isEmpty()) {
            return getForbiddenModelAndView(request);
        }

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

        boolean emailSendingFailed = false;
        try {
            if (!secureService.decrypt(password, secureService.getSecureKey(request.getRemoteAddr())).equals(
                    secureService.decrypt(confirmPassword, secureService.getSecureKey(request.getRemoteAddr())))) {
                modelAndView = MvcHelper.getErrorModelAndView(PAGE_REGISTRATION,
                        MessageKeys.PASSWORDS_MUST_BE_IDENTICAL_MESSAGE.getMessageKey(), lang);
                MvcHelper.addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                        secureService.getEncryptMethodNameForView(), lang);
                response.setStatus(400);
                return modelAndView;
            }

            if (!ValidationHelper.isValidEmail(email)) {
                modelAndView = MvcHelper.getErrorModelAndView(PAGE_REGISTRATION,
                        MessageKeys.EMAIL_NOT_VALID_MESSAGE.getMessageKey(), lang);
                MvcHelper.addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                        secureService.getEncryptMethodNameForView(), lang);
                response.setStatus(400);
                return modelAndView;
            }

            String decodedPassword = secureService.decrypt(password,
                    secureService.getSecureKey(request.getRemoteAddr()));

            if (!ValidationHelper.isValidPassword(mainConfig.getPasswordPattern(), decodedPassword)) {
                modelAndView = new ModelAndView("redirect:" + PAGE_REGISTRATION);
                modelAndView.addObject("error", true);
                modelAndView.addObject("errorMsg", mainConfig.getPasswordDoesntMatchPatternMessage());
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

            newUser = userService.registerUser(login, username, passwordEncoder.encode(decodedPassword),
                    email, registerRole);
            Utils.clearString(decodedPassword);
            String url = String.format("%s%s/%s?login=%s&verificationId=%s&lang=%s", mainConfig.getApplicationUrl(),
                    USER_CONTROLLER_PATH, PAGE_CONFIRM_USER, login, newUser.getVerificationId(), lang);
            try {
                emailService.sendSimpleMessage(email,
                        String.format("User registration at %s", mainConfig.getApplicationUrl()),
                        String.format("Confirm user registration: %s", url));
            } catch (MailException mailException) {
                log.error("Sending email error!", mailException);
                emailSendingFailed = true;
            }

        } catch (UserExistsException e) {
            modelAndView = MvcHelper.getErrorModelAndView(PAGE_REGISTRATION,
                    MessageKeys.USER_EXISTS_PATTERN.getMessageKey(), lang, username);
            MvcHelper.addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            response.setStatus(400);
            return modelAndView;
        } catch (Exception e) {

            modelAndView = MvcHelper.getErrorModelAndView(PAGE_REGISTRATION,
                    MessageKeys.CREATE_USER_ERROR_MESSAGE.getMessageKey(), lang);
            MvcHelper.addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            response.setStatus(500);
            return modelAndView;
        }

        modelAndView = getSuccessModelAndView(PAGE_REGISTRATION,
                MessageKeys.USER_CREATED_SUCCESSFULLY_PATTERN.getMessageKey(), lang, newUser.getLogin());
        modelAndView.addObject("emailSendingFailed", emailSendingFailed);

        MvcHelper.addObjectsToModelAndView(modelAndView, pageRegistrationParams, language,
                secureService.getEncryptMethodNameForView(), lang);

        return modelAndView;
    }

    @GetMapping(value = {"/" + PAGE_CHANGE_PASSWORD, "/" + PAGE_CHANGE_PASSWORD + ".html"},
            produces = {"application/json; charset=utf-8"})
    ModelAndView changeUserPassword(HttpServletRequest request, HttpServletResponse response,
                                           @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView = new ModelAndView(PAGE_CHANGE_PASSWORD);
        String authorizedName = "";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            authorizedName = authentication.getName();
        }
        if (!isAuthorizedUser(authorizedName)) {
            modelAndView = MvcHelper.getErrorModelAndView(PAGE_CHANGE_PASSWORD,
                    MessageKeys.AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
        }

        MvcHelper.addObjectsToModelAndView(modelAndView, pageChangePasswordParams, language,
                secureService.getEncryptMethodNameForView(), lang);
        MvcHelper.addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);
        return modelAndView;
    }

    @PatchMapping(PAGE_CHANGE_PASSWORD)
    ModelAndView performChangeUserPassword(HttpServletRequest request, HttpServletResponse response,
                                                  @RequestParam String currentPassword,
                                                  @RequestParam String newPassword,
                                                  @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView;
        try {
            String authorizedUserName = "";
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                authorizedUserName = authentication.getName();
            }
            if (isAuthorizedUser(authorizedUserName)) {
                if (authorizedUserName.toLowerCase().contains("anonymoususer")) {
                    modelAndView = MvcHelper.getErrorModelAndView(PAGE_CHANGE_PASSWORD,
                            MessageKeys.AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
                    MvcHelper.addObjectsToModelAndView(modelAndView, pageChangePasswordParams, language,
                            secureService.getEncryptMethodNameForView(), lang);
                    response.setStatus(403);
                    return modelAndView;
                }
                User user = userService.getUserByLogin(authorizedUserName);
                String realNewPassword = secureService.decrypt(newPassword,
                        secureService.getSecureKey(request.getRemoteAddr()));
                String realCurrentPassword = secureService.decrypt(currentPassword,
                        secureService.getSecureKey(request.getRemoteAddr()));
                if (user != null) {
                    if (passwordEncoder.matches(realCurrentPassword, user.getPassword())) {
                        user.setPassword(passwordEncoder.encode(realNewPassword));
                        userService.updateUser(user);
                    } else {
                        modelAndView = MvcHelper.getErrorModelAndView(PAGE_CHANGE_PASSWORD,
                                MessageKeys.WRONG_PASSWORD_MESSAGE.getMessageKey(), lang);
                        response.setStatus(500);
                        MvcHelper.addObjectsToModelAndView(modelAndView, pageChangePasswordParams, language,
                                secureService.getEncryptMethodNameForView(), lang);
                        return modelAndView;
                    }

                }
            } else {
                modelAndView = MvcHelper.getErrorModelAndView(PAGE_CHANGE_PASSWORD,
                        MessageKeys.AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
                response.setStatus(500);
                MvcHelper.addObjectsToModelAndView(modelAndView, pageChangePasswordParams, language,
                        secureService.getEncryptMethodNameForView(), lang);
                return modelAndView;
            }

        } catch (Exception ex) {
            modelAndView = MvcHelper.getErrorModelAndView(PAGE_CHANGE_PASSWORD,
                    MessageKeys.REQUEST_PROCESSING_ERROR.getMessageKey(), lang);
            MvcHelper.addObjectsToModelAndView(modelAndView, pageChangePasswordParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            response.setStatus(500);
            return modelAndView;
        }

        modelAndView = MvcHelper.getSuccessModelAndView(PAGE_CHANGE_PASSWORD,
                MessageKeys.PASSWORD_SUCCESSFULLY_CHANGED_MESSAGE.getMessageKey(), lang);
        response.setStatus(200);
        MvcHelper.addObjectsToModelAndView(modelAndView, pageChangePasswordParams, language,
                secureService.getEncryptMethodNameForView(), lang);
        MvcHelper.addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);

        return modelAndView;
    }

    @GetMapping(value = {"/" + PAGE_CONFIRM_USER, "/" + PAGE_CONFIRM_USER + ".html"})
    ModelAndView confirmUser(HttpServletRequest request, HttpServletResponse response,
                                           @RequestParam final String login,
                                           @RequestParam(required = false) String verificationId,
                                           @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView;
        boolean isAdminUser = false;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Collection<?> authorities = authentication.getAuthorities();
            for (Object authority : authorities) {
                if (authority.toString().equals("ROLE_ADMIN")) {
                    isAdminUser = true;
                }
            }
        }

        String resultMessage = LocaleConfig.getMessage(PAGE_CONFIRM_USER,
                MessageKeys.CONFIRM_SUCCESS_MESSAGE.getMessageKey(), lang);

        try {
            userService.confirmUser(login, verificationId, isAdminUser);
        } catch (Exception e) {
            resultMessage = LocaleConfig.getMessage(PAGE_CONFIRM_USER,
                    MessageKeys.CONFIRM_FAILED_MESSAGE.getMessageKey(), lang);
        }

        modelAndView = new ModelAndView(PAGE_CONFIRM_USER);
        modelAndView.addObject("confirmMessage", resultMessage);

        MvcHelper.addObjectsToModelAndView(modelAndView, pageConfirmUserParams, language,
                secureService.getEncryptMethodNameForView(), lang);
        MvcHelper.addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);
        return modelAndView;
    }


    @GetMapping(value = {"/" + PAGE_RECOVERY_PASSWORD, "/" + PAGE_RECOVERY_PASSWORD + ".html"})
    ModelAndView passwordRecovery(HttpServletRequest request,
                                         @RequestParam(required = false, defaultValue = "") final String recoveryId,
                                         @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView = new ModelAndView(PAGE_RECOVERY_PASSWORD);
        boolean successSend = true;
        if (!recoveryId.isEmpty()) {
            try {
                User user = userService.getUserByRecoveryId(recoveryId);
                String newPassword = userService.generateNewUserPassword(recoveryId);
                String message = "Your new password: " + newPassword;
                emailService.sendSimpleMessage(user.getEmail(), "Your new password: ", message);
            } catch (UsernameNotFoundException usernameNotFoundException) {
                log.error("User not found! ", usernameNotFoundException);
                modelAndView.addObject("userNotFound", true);
                successSend = false;
            } catch (MailException mailException) {
                log.error("Failed to send an email!", mailException);
                modelAndView.addObject("failedSend", true);
                successSend = false;
            }
            modelAndView.addObject("successSend", successSend);
        }

        MvcHelper.addObjectsToModelAndView(modelAndView, pagePasswordRecoveryParams, language,
                secureService.getEncryptMethodNameForView(), lang);
        MvcHelper.addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);
        return modelAndView;
    }


    @PostMapping(PERFORM_PASSWORD_RECOVERY)
    ModelAndView performPasswordRecovery(HttpServletRequest request,
                                                @RequestParam String loginOrEmail,
                                                @RequestParam(required = false) final String lang) {
        ModelAndView modelAndView = new ModelAndView("redirect:" + PAGE_RECOVERY_PASSWORD);
        boolean successSend = true;
        try {
            Map<String, User> map = userService.getUserRecoveryId(loginOrEmail);
            Optional<String> optionalRecoveryId = map.keySet().stream().findFirst();

            if (optionalRecoveryId.isPresent()) {
                User user = map.get(optionalRecoveryId.get());
                String message = mainConfig.getApplicationUrl() + USER_CONTROLLER_PATH + "/" +
                        PAGE_RECOVERY_PASSWORD + "?recoveryId=" + optionalRecoveryId.get();
                emailService.sendSimpleMessage(user.getEmail(), "Password recovery", message);
            }
        } catch (UsernameNotFoundException usernameNotFoundException) {
            log.error("User not found: " + loginOrEmail + "! ", usernameNotFoundException);
            modelAndView.addObject("userNotFound", true);
            successSend = false;
        } catch (MailException mailException) {
            log.error("Failed to send an email!", mailException);
            modelAndView.addObject("failedSend", true);
            successSend = false;
        }

        modelAndView.addObject("successSend", successSend);

        MvcHelper.addObjectsToModelAndView(modelAndView, pagePasswordRecoveryParams, language,
                secureService.getEncryptMethodNameForView(), lang);
        MvcHelper.addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);
        return modelAndView;
    }

    @GetMapping(value = {"/" + PAGE_USER_EDIT + "/{login}", "/" + PAGE_USER_EDIT + ".html" + "/{login}"})
    ModelAndView userEdit(HttpServletRequest request, HttpServletResponse response,
                                 @PathVariable String login,
                                 @RequestParam(required = false) final boolean success,
                                 @RequestParam(required = false) final String successMsg,
                                 @RequestParam(required = false) final boolean error,
                                 @RequestParam(required = false) final String errorMsg,
                                 @RequestParam(required = false) final String lang) {

        ModelAndView modelAndView = new ModelAndView(PAGE_USER_EDIT);
        User user;
        try {
            user = userService.getUserByLogin(login);

            if (!UserSecurityHelper.getAuthorizedUserLogin().equals(user.getLogin())) {
                modelAndView = MvcHelper.getErrorModelAndView(UmConfig.LOGIN_URL,
                        MessageKeys.ADMIN_AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
                MvcHelper.addObjectsToModelAndView(modelAndView, pageLoginParams, language,
                        secureService.getEncryptMethodNameForView(), lang);
                response.setStatus(403);
                MvcHelper.addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);
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

        modelAndView.addObject("user", user);

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

    @PatchMapping
    ModelAndView performUserEdit(HttpServletRequest request, HttpServletResponse response,
                                        @ModelAttribute UserDto userDto,
                                        @RequestParam(required = false) final String lang) {

        ModelAndView modelAndView = new ModelAndView(PAGE_USER_EDIT);
        User newUser;
        try {
            User userFromDatabase = userService.getUserById(userDto.getId());

            if (!UserSecurityHelper.getAuthorizedUserLogin().equals(userFromDatabase.getLogin())) {
                modelAndView = MvcHelper.getErrorModelAndView(UmConfig.LOGIN_URL,
                        MessageKeys.ADMIN_AUTHENTICATION_REQUIRED_MESSAGE.getMessageKey(), lang);
                MvcHelper.addObjectsToModelAndView(modelAndView, pageLoginParams, language,
                        secureService.getEncryptMethodNameForView(), lang);
                response.setStatus(403);
                MvcHelper.addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);
                modelAndView.setViewName(modelAndView.getViewName() + "/" + userDto.getLogin());
                return modelAndView;
            }

            if (!ValidationHelper.isValidEmail(userDto.getEmail())) {
                modelAndView = MvcHelper.getErrorModelAndView(PAGE_USER_EDIT,
                        MessageKeys.EMAIL_NOT_VALID_MESSAGE.getMessageKey(), lang);
                MvcHelper.addObjectsToModelAndView(modelAndView, pageUserEditParams, language,
                        secureService.getEncryptMethodNameForView(), lang);
                modelAndView.setViewName(modelAndView.getViewName() + "/" + userFromDatabase.getLogin());
                response.setStatus(400);
                return modelAndView;
            }

            newUser = User.builder().id(userFromDatabase.getId()).login(userFromDatabase.getLogin())
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
        } catch (Exception e) {
            log.error("User update error! ", e);
            modelAndView = MvcHelper.getErrorModelAndView(PAGE_USER_EDIT,
                    MessageKeys.USER_EDIT_ERROR_MESSAGE.getMessageKey(), lang);
            MvcHelper.addObjectsToModelAndView(modelAndView, pageUserEditParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            return modelAndView;
        }

        modelAndView = new ModelAndView("redirect:" + USER_CONTROLLER_PATH +
                "/" +  PAGE_USER_EDIT + "/" + newUser.getLogin());

        MvcHelper.addObjectsToModelAndView(modelAndView, PAGE_USER_EDIT, pageUserEditParams, language,
                secureService.getEncryptMethodNameForView(), lang);
        MvcHelper.addObjectsToModelAndView(modelAndView, request.getParameterMap(), IGNORED_PARAMS);

        String successMsg = LocaleConfig.getMessage(PAGE_USER_EDIT,
                MessageKeys.USER_EDIT_SUCCESS_MESSAGE.getMessageKey(), lang);
        modelAndView.addObject("success", true);
        modelAndView.addObject("successMsg", successMsg);
        return modelAndView;
    }

    @GetMapping(value = {"/" + PAGE_USER_CONTROL_PANEL, "/" + PAGE_USER_CONTROL_PANEL + ".html"})
    ModelAndView userControlPanel(HttpServletRequest request, HttpServletResponse response,
                                 @RequestParam(required = false) final boolean success,
                                 @RequestParam(required = false) final String successMsg,
                                 @RequestParam(required = false) final boolean error,
                                 @RequestParam(required = false) final String errorMsg,
                                 @RequestParam(required = false) final String lang) {

        ModelAndView modelAndView = new ModelAndView(PAGE_USER_CONTROL_PANEL);
        User user;
        try {
            String login = UserSecurityHelper.getAuthorizedUserLogin();
            user = userService.getUserByLogin(login);
        } catch (Exception e) {
            log.error("User update error! ", e);
            modelAndView = MvcHelper.getErrorModelAndView(UmConfig.LOGIN_URL,
                    MessageKeys.USER_EDIT_ERROR_MESSAGE.getMessageKey(), lang);
            MvcHelper.addObjectsToModelAndView(modelAndView, pageUserControlPanelParams, language,
                    secureService.getEncryptMethodNameForView(), lang);
            return modelAndView;
        }

        modelAndView.addObject("user", user);

        MvcHelper.addObjectsToModelAndView(modelAndView, pageUserControlPanelParams, language,
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

    private ModelAndView getForbiddenModelAndView(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null) {
            referer = UmConfig.successUrl;
        }
        ModelAndView modelAndView = new ModelAndView("redirect:" + referer);
        modelAndView.setStatus(HttpStatus.FORBIDDEN);
        return modelAndView;
    }

    private boolean isAuthorizedUser(String userName) {
        return (userName != null && !userName.isEmpty());
    }

}

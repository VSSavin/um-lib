package com.github.vssavin.umlib.base.controller;

import com.github.vssavin.umlib.config.LocaleConfig;
import com.github.vssavin.umlib.config.UmConfig;
import com.github.vssavin.umlib.domain.language.MessageKey;
import com.github.vssavin.umlib.domain.language.UmLanguage;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Base class for project controllers that use
 * {@link org.springframework.web.servlet.ModelAndView} as their return type.
 * Contains methods for adding various objects to model map  with predefined
 * attribute names using specified language.
 *
 * @author vssavin on 12.07.2023
 */
public class UmControllerBase {
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    protected static final String REDIRECT_PREFIX = "redirect:";

    protected static final String ERROR_PAGE = "errorPage";

    protected static final String ERROR_ATTRIBUTE = "error";

    protected static final String ERROR_MSG_ATTRIBUTE = "errorMsg";

    protected static final String SUCCESS_ATTRIBUTE = "success";

    protected static final String SUCCESS_MSG_ATTRIBUTE = "successMsg";

    protected static final String USER_NAME_ATTRIBUTE = "userName";

    protected static final String USERS_ATTRIBUTE = "users";

    protected static final String USER_ATTRIBUTE = "user";

    protected final UmLanguage language;
    protected final UmConfig umConfig;

    /**
     * Creates a new {@link UmControllerBase} with {@link UmLanguage} and {@link UmConfig} params.
     *
     * @param language UmLanguage instance
     * @param umConfig main configuration class
     */
    public UmControllerBase(UmLanguage language, UmConfig umConfig) {
        this.language = language;
        this.umConfig = umConfig;
    }

    /**
     * Adds all elements to {@link org.springframework.web.servlet.ModelAndView} instance
     * using requested language.
     *
     * @param modelAndView {@link org.springframework.web.servlet.ModelAndView} instance to add all elements
     * @param elements collection to be added to
     *                 modelAndView {@link org.springframework.web.servlet.ModelAndView} instance
     * @param encryptMethodName javascript method name to encrypt data
     * @param requestedLang language for elements
     */
    protected void addObjectsToModelAndView(ModelAndView modelAndView, Collection<String> elements,
                                            String encryptMethodName, String requestedLang) {
        if (modelAndView != null) {
            String page = getPageName(modelAndView);

            if (elements != null && !page.isEmpty()) {
                elements.forEach(param ->
                        modelAndView.addObject(param, LocaleConfig.getMessage(page, param, requestedLang)));
            }

            if (requestedLang != null) {
                modelAndView.addObject("lang", requestedLang);
                modelAndView.addObject("urlLang", requestedLang);
            }

            modelAndView.addObject("encryptMethodName", encryptMethodName);

            modelAndView.addObject("languages", LocaleConfig.getAvailableLanguages());
            if (language != null) {
                modelAndView.addObject("langObject", language);
            }
        }
    }

    /**
     * Adds all elements to {@link org.springframework.web.servlet.ModelAndView} instance
     * using requested language and specified viewName.
     *
     * @param modelAndView {@link org.springframework.web.servlet.ModelAndView} instance to add all elements
     * @param viewName view name for the specified language
     * @param elements collection to be added to
     *                 modelAndView {@link org.springframework.web.servlet.ModelAndView} instance
     * @param encryptMethodName javascript method name to encrypt data
     * @param requestedLang language for elements
     * @see #addObjectsToModelAndView(ModelAndView, Collection, String, String)
     */
    protected void addObjectsToModelAndView(ModelAndView modelAndView, String viewName, Collection<String> elements,
                                            String encryptMethodName, String requestedLang) {

        if (viewName == null) {
            addObjectsToModelAndView(modelAndView, elements, encryptMethodName, requestedLang);
        } else {
            if (elements != null) {
                elements.forEach(param ->
                        modelAndView.addObject(param, LocaleConfig.getMessage(viewName, param, requestedLang)));
            }

            if (requestedLang != null) {
                modelAndView.addObject("lang", requestedLang);
                modelAndView.addObject("urlLang", requestedLang);
            }

            modelAndView.addObject("encryptMethodName", encryptMethodName);

            modelAndView.addObject("languages", LocaleConfig.getAvailableLanguages());
            if (language != null) {
                modelAndView.addObject("langObject", language);
            }
        }
    }

    /**
     * Adds all elements from the requestMap to {@link org.springframework.web.servlet.ModelAndView} instance
     * except for elements from the ignored collection
     *
     * @param modelAndView {@link org.springframework.web.servlet.ModelAndView} instance to add all elements
     * @param requestMap the elements to add to modelAndView
     * @param ignored ignored elements
     */
    protected void addObjectsToModelAndView(ModelAndView modelAndView, Map<String, String[]> requestMap,
                                            Collection<String> ignored) {
        boolean ignoredEmpty = ignored == null;
        requestMap.forEach((key, value) -> {
            if (ignoredEmpty) {
                if (value.length == 1) {
                    modelAndView.addObject(key, value[0]);
                } else {
                    modelAndView.addObject(key, value);
                }
            } else {
                if (!ignored.contains(key)) {
                    if (value.length == 1) {
                        modelAndView.addObject(key, value[0]);
                    } else {
                        modelAndView.addObject(key, value);
                    }
                }
            }
        });
    }

    /**
     * Generates a {@link org.springframework.web.servlet.ModelAndView} object for the success response
     * using requested language
     *
     * @param page redirect page
     * @param messageKey the message key for specified language
     * @param lang the language
     * @param formatValues values to format the message string
     * @return the {@link org.springframework.web.servlet.ModelAndView} object with success params
     */
    protected ModelAndView getSuccessModelAndView(String page, MessageKey messageKey, String lang, Object... formatValues) {
        ModelAndView modelAndView = new ModelAndView(REDIRECT_PREFIX + page);
        modelAndView.addObject(SUCCESS_ATTRIBUTE, true);
        String message = LocaleConfig.getMessage(page.replaceFirst("/", ""), messageKey.getKey(), lang);
        if (message.contains("%")) {
            if (formatValues != null) {
                modelAndView.addObject(SUCCESS_MSG_ATTRIBUTE, String.format(message, formatValues));
            }

        } else {
            modelAndView.addObject(SUCCESS_MSG_ATTRIBUTE, message);
        }

        return modelAndView;
    }

    /**
     * Generates a {@link org.springframework.web.servlet.ModelAndView} object for the error response
     * using requested language
     *
     * @param page redirect page
     * @param messageKey the message key for specified language
     * @param lang the language
     * @param formatValues values to format the message string
     * @return the {@link org.springframework.web.servlet.ModelAndView} object with error params
     */
    protected ModelAndView getErrorModelAndView(String page, MessageKey messageKey, String lang, Object... formatValues) {
        ModelAndView modelAndView = new ModelAndView(REDIRECT_PREFIX + page);
        modelAndView.addObject(ERROR_ATTRIBUTE, true);
        String message = LocaleConfig.getMessage(page.replaceFirst("/", ""), messageKey.getKey(), lang);
        if (message.contains("%")) {
            if (formatValues != null) {
                modelAndView.addObject(ERROR_MSG_ATTRIBUTE, String.format(message, formatValues));
            }
        } else {
            modelAndView.addObject(ERROR_MSG_ATTRIBUTE, message);
        }

        return modelAndView;
    }

    /**
     * Generates a {@link org.springframework.web.servlet.ModelAndView} object for the forbidden response
     *
     * @param request the request to check headers
     * @return the {@link org.springframework.web.servlet.ModelAndView} object with forbidden params
     */
    protected ModelAndView getForbiddenModelAndView(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null) {
            referer = umConfig.getSuccessUrl();
        }
        ModelAndView modelAndView = new ModelAndView(REDIRECT_PREFIX + referer);
        modelAndView.setStatus(HttpStatus.FORBIDDEN);
        return modelAndView;
    }

    /**
     * Checks if the user authorized
     *
     * @param userName username to check
     * @return true if user authorized, false otherwise
     */
    protected boolean isAuthorizedUser(String userName) {
        return (userName != null && !userName.isEmpty());
    }

    /**
     * Checks if email is valid
     *
     * @param emailStr email to check
     * @return true if email is valid, false otherwise
     */
    protected boolean isValidUserEmail(String emailStr) {
        return VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr).matches();
    }

    /**
     * Checks if password is valid
     *
     * @param validRegexPattern password pattern
     * @param passwordStr password to check
     * @return true if the password matches the pattern, false otherwise
     */
    protected boolean isValidUserPassword(Pattern validRegexPattern, String passwordStr) {
        return validRegexPattern.matcher(passwordStr).matches();
    }

    /**
     * Returns the name of the modelAndView
     *
     * @param modelAndView the instance of {@link org.springframework.web.servlet.ModelAndView}
     * @return the string name of the modelAndView object, empty if the name is not found
     */
    private String getPageName(ModelAndView modelAndView) {
        String[] splitted = new String[0];
        String viewName = modelAndView.getViewName();
        if (viewName != null) {
            splitted = viewName.split(":");
        }

        String page;
        if (viewName == null) {
            page = "";
        } else {
            page = splitted.length > 1 ? splitted[1].replaceFirst("/", "") : splitted[0];
        }

        return page;
    }
}

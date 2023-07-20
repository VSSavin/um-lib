package com.github.vssavin.umlib.base;

import com.github.vssavin.umlib.config.LocaleConfig;
import com.github.vssavin.umlib.config.UmConfig;
import com.github.vssavin.umlib.language.UmLanguage;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

/**
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

    public UmControllerBase(UmLanguage language, UmConfig umConfig) {
        this.language = language;
        this.umConfig = umConfig;
    }

    protected void addObjectsToModelAndView(ModelAndView modelAndView, Collection<String> elements,
                                            String encryptMethodName, String requestedLang) {
        if (modelAndView != null) {
            String page = getPageName(modelAndView);

            if (elements != null && !page.isEmpty()) {
                elements.forEach(param ->
                        modelAndView.addObject(param,LocaleConfig.getMessage(page, param, requestedLang)));
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

    protected ModelAndView getSuccessModelAndView(String page, String messageKey, String lang, Object... formatValues) {
        ModelAndView modelAndView = new ModelAndView(REDIRECT_PREFIX + page);
        modelAndView.addObject(SUCCESS_ATTRIBUTE, true);
        String message = LocaleConfig.getMessage(page.replaceFirst("/", ""), messageKey, lang);
        if (message.contains("%")) {
            if (formatValues != null) {
                modelAndView.addObject(SUCCESS_MSG_ATTRIBUTE, String.format(message, formatValues));
            }

        } else {
            modelAndView.addObject(SUCCESS_MSG_ATTRIBUTE, message);
        }

        return modelAndView;
    }

    protected ModelAndView getErrorModelAndView(String page, String messageKey, String lang, Object... formatValues) {
        ModelAndView modelAndView = new ModelAndView(REDIRECT_PREFIX + page);
        modelAndView.addObject(ERROR_ATTRIBUTE, true);
        String message = LocaleConfig.getMessage(page.replaceFirst("/", ""), messageKey, lang);
        if (message.contains("%")) {
            if (formatValues != null) {
                modelAndView.addObject(ERROR_MSG_ATTRIBUTE, String.format(message, formatValues));
            }
        } else {
            modelAndView.addObject(ERROR_MSG_ATTRIBUTE, message);
        }

        return modelAndView;
    }

    protected ModelAndView getForbiddenModelAndView(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null) {
            referer = umConfig.getSuccessUrl();
        }
        ModelAndView modelAndView = new ModelAndView(REDIRECT_PREFIX + referer);
        modelAndView.setStatus(HttpStatus.FORBIDDEN);
        return modelAndView;
    }

    protected boolean isAuthorizedUser(String userName) {
        return (userName != null && !userName.isEmpty());
    }

    protected boolean isValidUserEmail(String emailStr) {
        return VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr).matches();
    }

    protected boolean isValidUserPassword(Pattern validRegexPattern, String passwordStr) {
        return validRegexPattern.matcher(passwordStr).matches();
    }

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

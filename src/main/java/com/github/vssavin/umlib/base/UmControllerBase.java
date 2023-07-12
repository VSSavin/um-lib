package com.github.vssavin.umlib.base;

import com.github.vssavin.umlib.config.LocaleConfig;
import com.github.vssavin.umlib.config.UmConfig;
import com.github.vssavin.umlib.language.UmLanguage;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author vssavin on 12.07.2023
 */
public class UmControllerBase {
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    protected final UmLanguage language;

    public UmControllerBase(UmLanguage language) {
        this.language = language;
    }

    protected void addObjectsToModelAndView(ModelAndView modelAndView, Collection<String> elements,
                                            String encryptMethodName, String requestedLang) {
        if (Objects.nonNull(modelAndView)) {
            String[] splitted = new String[0];
            if (Objects.nonNull(modelAndView.getViewName())) {
                splitted = modelAndView.getViewName().split(":");
            }

            String page = Objects.isNull(modelAndView.getViewName()) ? "" : splitted.length > 1 ?
                    splitted[1].replaceFirst("/", "") : splitted[0];

            if (Objects.nonNull(elements) && !page.isEmpty()) {
                elements.forEach(param ->
                        modelAndView.addObject(param,LocaleConfig.getMessage(page, param, requestedLang)));
            }

            if (Objects.nonNull(requestedLang)) {
                modelAndView.addObject("lang", requestedLang);
                modelAndView.addObject("urlLang", requestedLang);
            }

            modelAndView.addObject("encryptMethodName", encryptMethodName);

            modelAndView.addObject("languages", LocaleConfig.getAvailableLanguages());
            if (Objects.nonNull(language)) {
                modelAndView.addObject("langObject", language);
            }
        }
    }

    protected void addObjectsToModelAndView(ModelAndView modelAndView, String viewName, Collection<String> elements,
                                            String encryptMethodName, String requestedLang) {

        if (Objects.isNull(viewName)) {
            addObjectsToModelAndView(modelAndView, elements, encryptMethodName, requestedLang);
        } else {
            if (Objects.nonNull(elements)) {
                elements.forEach(param ->
                        modelAndView.addObject(param, LocaleConfig.getMessage(viewName, param, requestedLang)));
            }

            if (Objects.nonNull(requestedLang)) {
                modelAndView.addObject("lang", requestedLang);
                modelAndView.addObject("urlLang", requestedLang);
            }

            modelAndView.addObject("encryptMethodName", encryptMethodName);

            modelAndView.addObject("languages", LocaleConfig.getAvailableLanguages());
            if (Objects.nonNull(language)) {
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
        ModelAndView modelAndView = new ModelAndView("redirect:" + page);
        modelAndView.addObject("success", true);
        String message = LocaleConfig.getMessage(page.replaceFirst("/", ""), messageKey, lang);
        if (message.contains("%")) {
            if (formatValues != null) {
                modelAndView.addObject("successMsg", String.format(message, formatValues));
            }

        } else {
            modelAndView.addObject("successMsg", message);
        }

        return modelAndView;
    }

    protected ModelAndView getErrorModelAndView(String page, String messageKey, String lang, Object... formatValues) {
        ModelAndView modelAndView = new ModelAndView("redirect:" + page);
        modelAndView.addObject("error", true);
        String message = LocaleConfig.getMessage(page.replaceFirst("/", ""), messageKey, lang);
        if (message.contains("%")) {
            if (formatValues != null) {
                modelAndView.addObject("errorMsg", String.format(message, formatValues));
            }
        } else {
            modelAndView.addObject("errorMsg", message);
        }

        return modelAndView;
    }

    protected ModelAndView getForbiddenModelAndView(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null) {
            referer = UmConfig.successUrl;
        }
        ModelAndView modelAndView = new ModelAndView("redirect:" + referer);
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
}

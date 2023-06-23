package io.github.vssavin.umlib.helper;

import io.github.vssavin.umlib.config.LocaleConfig;
import io.github.vssavin.umlib.language.UmLanguage;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;
import java.util.Map;

/**
 * @author vssavin on 22.12.21
 */
public class MvcHelper {

    public static void addObjectsToModelAndView(ModelAndView modelAndView, Collection<String> elements,
                                                UmLanguage language, String encryptMethodName,
                                                String requestedLang) {
        if (modelAndView != null) {
            String page = modelAndView.getViewName();
            if (page != null) {
                String[] splitted = page.split(":");
                if (splitted.length > 1) {
                    page = splitted[1];
                }
            }

            if (elements != null && page != null) {
                for(String param : elements) {
                    modelAndView.addObject(param,
                            LocaleConfig.getMessage(page.replaceFirst("/", ""), param, requestedLang));
                }
            }

            if (requestedLang != null) {
                modelAndView.addObject("lang", requestedLang);
                modelAndView.addObject("urlLang", requestedLang);
            }

            modelAndView.addObject("encryptMethodName", encryptMethodName);

            modelAndView.addObject("languages", LocaleConfig.getAvailableLanguages());
            if (language != null) modelAndView.addObject("langObject", language);

        }
    }

    public static void addObjectsToModelAndView(ModelAndView modelAndView, String viewName, Collection<String> elements,
                                                UmLanguage language, String encryptMethodName,
                                                String requestedLang) {

        if (viewName == null) {
            addObjectsToModelAndView(modelAndView, elements, language, encryptMethodName, requestedLang);
        }

        else {
            if (elements != null) {
                for(String param : elements) {
                    modelAndView.addObject(param, LocaleConfig.getMessage(viewName, param, requestedLang));
                }
            }

            if (requestedLang != null) {
                modelAndView.addObject("lang", requestedLang);
                modelAndView.addObject("urlLang", requestedLang);
            }

            modelAndView.addObject("encryptMethodName", encryptMethodName);

            modelAndView.addObject("languages", LocaleConfig.getAvailableLanguages());
            if (language != null) modelAndView.addObject("langObject", language);
        }
    }

    public static void addObjectsToModelAndView(ModelAndView modelAndView, Map<String, String[]> requestMap,
                                                Collection<String> ignored) {
        boolean ignoredEmpty = ignored == null;
        for(Map.Entry<String, String[]> entry : requestMap.entrySet()) {
            String[] values = entry.getValue();
            if (ignoredEmpty) {
                if (values.length == 1) {
                    modelAndView.addObject(entry.getKey(), values[0]);
                } else {
                    modelAndView.addObject(entry.getKey(), entry.getValue());
                }
            }

            else {
                if(!ignored.contains(entry.getKey())) {
                    if (values.length == 1) {
                        modelAndView.addObject(entry.getKey(), values[0]);
                    } else {
                        modelAndView.addObject(entry.getKey(), entry.getValue());
                    }
                }
            }

        }
    }

    public static ModelAndView getSuccessModelAndView(String page, String messageKey, String lang,
                                                      Object... formatValues) {
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

    public static ModelAndView getErrorModelAndView(String page, String messageKey, String lang,
                                                    Object... formatValues) {
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
}

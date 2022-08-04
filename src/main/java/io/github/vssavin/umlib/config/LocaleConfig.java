package io.github.vssavin.umlib.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author vssavin on 27.12.21
 */
@Configuration
public class LocaleConfig {
    private static final String DEFAULT_LANGUAGE = "ru";
    public static final Locale DEFAULT_LOCALE = Locale.forLanguageTag(DEFAULT_LANGUAGE);
    public static final Map<String, String> AVAILABLE_LANGUAGES = new LinkedHashMap<>();
    private static final Map<String, String> LANGUAGE_NAMES = new HashMap<>();
    private static final Map<String, String> FLAGS_MAP = new HashMap<>();


    private static final Map<String, LocaleSpringMessageSource> messageSourceMap = new HashMap<>();

    static {
        LANGUAGE_NAMES.put("en", "English");
        LANGUAGE_NAMES.put("ru", "Русский");

        AVAILABLE_LANGUAGES.put(DEFAULT_LANGUAGE, LANGUAGE_NAMES.get(DEFAULT_LANGUAGE));
    }

    static {
        initFlagsMap();
        initAvailableLanguagesMap();
    }

    @Component
    public static class LocaleSpringMessageSource extends ReloadableResourceBundleMessageSource {

        public Set<String> getKeys() {
            HashSet<String> keySet = new HashSet<>();
            PropertiesHolder holder = super.getMergedProperties(DEFAULT_LOCALE);
            Properties props = holder.getProperties();
            if (props == null) return new HashSet<>();
            else {
                for(Object key : props.keySet()) {
                    if (key instanceof String) keySet.add((String) key);
                }
                return keySet;
            }
        }
    }

    public static String getFlagName(String localeString, boolean returnFirstValueIfNotExists) {
        String flagName = FLAGS_MAP.get(localeString);
        if (flagName == null) {
            if (returnFirstValueIfNotExists) {
                flagName = FLAGS_MAP.values().iterator().next();
                if (flagName == null) return "";
            }
             else {
                 return "";
            }
        }

        return flagName;
    }

    public static String getAvailableLanguageName(String localeString, boolean returnDefaultIfNotExists) {
        String languageName = AVAILABLE_LANGUAGES.get(localeString);
        if (languageName == null) {
            if (returnDefaultIfNotExists) {
                languageName = AVAILABLE_LANGUAGES.get(DEFAULT_LANGUAGE);
                if (languageName == null) return "";
            }
            else {
                return "";
            }
        }
         return languageName;
    }

    public static String getAvailableLocale(String localeString) {
        String availableLanguage = AVAILABLE_LANGUAGES.get(localeString);
        String availableLocale;
        if (availableLanguage == null) {
            availableLocale = DEFAULT_LANGUAGE;
        } else {
            availableLocale = localeString;
        }
        return availableLocale;
    }

    public static String getMessage(String page, String key, String localeString) {
        LocaleSpringMessageSource messageSource = messageSourceMap.get(page);
        String locale = localeString == null ? DEFAULT_LANGUAGE : localeString;
        return messageSource.getMessage(key, new Object[]{}, Locale.forLanguageTag(locale));
    }

    @Bean
    public LocaleSpringMessageSource adminMessageSource() {
        return createMessageSource("admin");
    }

    @Bean
    public LocaleSpringMessageSource usersMessageSource() {
        return createMessageSource("users");
    }

    @Bean
    public LocaleSpringMessageSource loginMessageSource() {
        return createMessageSource("login");
    }

    @Bean
    public LocaleSpringMessageSource registrationMessageSource() {
        return createMessageSource("registration");
    }

    @Bean
    public LocaleSpringMessageSource changeUserPasswordMessageSource() {
        return createMessageSource("changeUserPassword");
    }

    @Bean
    public LocaleSpringMessageSource confirmUserMessageSource() {
        return createMessageSource("confirmUser");
    }

    @Bean
    public LocaleSpringMessageSource adminConfirmUserMessageSource() {
        return createMessageSource("adminConfirmUser");
    }

    @Bean
    public LocaleSpringMessageSource changePasswordMessageSource() {
        return createMessageSource("changePassword");
    }

    @Bean
    public LocaleSpringMessageSource passwordRecoveryMessageSource() {
        return createMessageSource("passwordRecovery");
    }

    @Bean
    public LocaleSpringMessageSource logoutMessageSource() {
        return createMessageSource("logout");
    }

    private static void initFlagsMap() {
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle("language.config");
            String flags = bundle.getString("flags");

            String[] flagsArray = flags.split(";");
            for(String flagParams : flagsArray) {
                String[] flag = flagParams.split(":");
                if (flag.length > 1) {
                    FLAGS_MAP.put(flag[0].trim(), flag[1].trim());
                }
            }
        } catch (MissingResourceException e) {
            FLAGS_MAP.put("en", "flag-icon-usa");
        }
    }

    private static void initAvailableLanguagesMap() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:language/index/language");
        messageSource.setDefaultEncoding("UTF-8");
        String defaultMessage = "Язык";
        for(Locale locale : Locale.getAvailableLocales()) {
            String message = messageSource.getMessage("language", new Object[]{}, locale);
            if (!defaultMessage.equals(message)) {
                String lang = locale.getLanguage();
                AVAILABLE_LANGUAGES.put(lang, LANGUAGE_NAMES.get(lang));
            }
        }
    }

    private LocaleSpringMessageSource createMessageSource(String page) {
        LocaleSpringMessageSource messageSource = new LocaleSpringMessageSource();
        messageSource.setBasename(String.format("classpath:language/%s/language", page));
        messageSource.setDefaultEncoding("UTF-8");
        messageSourceMap.put(page, messageSource);
        return messageSource;
    }
}

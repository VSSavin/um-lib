package com.github.vssavin.umlib.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Configuration class for automatically generation of required language beans.
 *
 * @author vssavin on 27.12.21
 */
@Configuration
public class LocaleConfig {
    private static final Logger log = LoggerFactory.getLogger(LocaleConfig.class);
    private static final LocaleSpringMessageSource EMPTY = new LocaleSpringMessageSource();
    private static final String DEFAULT_LANGUAGE = "ru";
    public static final Locale DEFAULT_LOCALE = Locale.forLanguageTag(DEFAULT_LANGUAGE);
    private static final Map<String, String> AVAILABLE_LANGUAGES = new LinkedHashMap<>();
    private static final Map<String, String> FLAGS_MAP = new HashMap<>();

    private static final Map<String, LocaleSpringMessageSource> messageSourceMap = new HashMap<>();

    static {
        initFlagsMap();
        initAvailableLanguagesMap();
    }

    private final ConfigurableBeanFactory beanFactory;
    private final Map<String, LocaleSpringMessageSource> languageBeans = new HashMap<>();

    @Autowired
    public LocaleConfig(ConfigurableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        createLanguageBeans();
    }

    public LocaleSpringMessageSource forPage(String page) {
        return languageBeans.getOrDefault(page + "MessageSource", EMPTY);
    }

    public static class LocaleSpringMessageSource extends ReloadableResourceBundleMessageSource {

        public Set<String> getKeys() {
            HashSet<String> keySet = new HashSet<>();
            PropertiesHolder holder = super.getMergedProperties(DEFAULT_LOCALE);
            Properties props = holder.getProperties();
            if (props == null) {
                return new HashSet<>();
            } else {
                for (Object key : props.keySet()) {
                    if (key instanceof String) {
                        keySet.add((String) key);
                    }
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
                if (flagName == null) {
                    return "";
                }
            } else {
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
                if (languageName == null) {
                    return "";
                }
            } else {
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

    public static Map<String, String> getAvailableLanguages() {
        return AVAILABLE_LANGUAGES;
    }

    public Map<String, LocaleSpringMessageSource> getLanguageBeans() {
        return languageBeans;
    }

    private void createLanguageBeans() {
        List<String> sources = getLanguageSources();
        sources.forEach(source -> {
            String sourceName = source + "MessageSource";
            LocaleSpringMessageSource languageMessageSource = createMessageSource(source);
            beanFactory.registerSingleton(sourceName, languageMessageSource);
            languageBeans.put(sourceName, languageMessageSource);
        });
    }

    private List<String> getLanguageSources() {
        try {
            Resource[] resources =
                    new PathMatchingResourcePatternResolver().getResources("classpath:language/*");
            if (resources.length > 0) {
                return Arrays.stream(resources).map(Resource::getFilename)
                        .filter(filename -> !Objects.requireNonNull(filename).endsWith(".properties"))
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            log.error("Error getting language resource: ", e);
        }
        return new ArrayList<>();
    }

    private static void initFlagsMap() {
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle("language.config");
            String flags = bundle.getString("flags");

            String[] flagsArray = flags.split(";");
            for (String flagParams : flagsArray) {
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
        messageSource.setBasename("classpath:language/config");
        messageSource.setDefaultEncoding("UTF-8");
        String messageLanguages = messageSource.getMessage("languages", new Object[]{}, Locale.getDefault());
        String[] languagesArray = messageLanguages.split(";");
        for (String flagParams : languagesArray) {
            String[] language = flagParams.split(":");
            if (language.length > 1) {
                AVAILABLE_LANGUAGES.put(language[0].trim(), language[1].trim());
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

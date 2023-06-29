package com.github.vssavin.umlib.language;

import com.github.vssavin.umlib.config.LocaleConfig;
import org.springframework.stereotype.Component;

/**
 * @author vssavin on 20.12.21
 */
@Component
public class UmLanguage {

    public String getLanguageSpan(String localeString) {
        String availableLocale = LocaleConfig.getAvailableLocale(localeString.toLowerCase());
        String languageName = LocaleConfig.getAvailableLanguageName(availableLocale, true);
        String flagIconName = LocaleConfig.getFlagName(availableLocale, true);
        return String.format("<span class=\"flag-icon %s\"></span> %s", flagIconName, languageName);
    }

    public String getLanguageText(String localeString) {
        return LocaleConfig.getAvailableLanguageName(localeString, true);
    }

}

package com.github.vssavin.umlib.language;

import com.github.vssavin.umlib.config.LocaleConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by vssavin on 16.05.2022.
 */
@RestController
class LanguageController {

    @GetMapping("/um/languages")
    ResponseEntity<?> getLanguage() {
        return new ResponseEntity<>(LocaleConfig.getAvailableLanguages(), HttpStatus.OK);
    }
}

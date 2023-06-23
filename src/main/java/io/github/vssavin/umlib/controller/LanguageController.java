package io.github.vssavin.umlib.controller;

import io.github.vssavin.umlib.config.LocaleConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by vssavin on 16.05.2022.
 */
@RestController
public class LanguageController {

    @GetMapping("/um/languages")
    public ResponseEntity<?> getLanguage() {
        return new ResponseEntity<>(LocaleConfig.getAvailableLanguages(), HttpStatus.OK);
    }
}

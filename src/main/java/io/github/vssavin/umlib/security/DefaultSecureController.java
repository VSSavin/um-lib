package io.github.vssavin.umlib.security;

import io.github.vssavin.umlib.config.UmConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by vssavin on 16.05.2022.
 */
@RestController
@RequestMapping("/secure")
class DefaultSecureController {
    private final SecureService secureService;

    public DefaultSecureController(UmConfig umConfig) {
        this.secureService = umConfig.getAuthService();
    }

    @GetMapping(value = "/key")
    public ResponseEntity<String> secureKey(HttpServletRequest request) {
        String addr = request.getRemoteAddr();
        return new ResponseEntity<>(secureService.getSecureKey(addr), HttpStatus.OK);
    }
}

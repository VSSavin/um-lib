package com.github.vssavin.umlib.security;

import com.github.vssavin.umlib.config.UmConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides data for using secure algorithms.
 *
 * Created by vssavin on 16.05.2022.
 */
@RestController
@RequestMapping("/um/security")
class SecurityController {
    private final SecureService secureService;
    private final List<String> secureServiceNames;

    @Autowired
    public SecurityController(UmConfig umConfig, List<SecureService> secureServices) {
        this.secureService = umConfig.getAuthService();
        this.secureServiceNames = secureServices.stream().map(Object::toString).collect(Collectors.toList());
    }

    @GetMapping(value = "/key")
    public ResponseEntity<String> key(HttpServletRequest request) {
        String addr = request.getRemoteAddr();
        return new ResponseEntity<>(secureService.getSecureKey(addr), HttpStatus.OK);
    }

    @GetMapping(value = "/algorithm")
    public ResponseEntity<String> algorithm() {
        return new ResponseEntity<>(secureService.toString(), HttpStatus.OK);
    }

    @GetMapping(value = "/algorithms")
    public ResponseEntity<List<String>> algorithms() {
        return new ResponseEntity<>(secureServiceNames, HttpStatus.OK);
    }

}

package io.github.vssavin.umlib.controller;

import io.github.vssavin.umlib.service.SecureService;
import io.github.vssavin.umlib.utils.UmUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by vssavin on 16.05.2022.
 */
@RequestMapping("/secure")
public class DefaultSecureController {
    private final SecureService secureService;

    public DefaultSecureController(UmUtil umUtil) {
        this.secureService = umUtil.getAuthService();
    }

    @GetMapping(value = "/key")
    public ResponseEntity<String> secureKey(HttpServletRequest request, HttpServletResponse response) {
        String addr = request.getRemoteAddr();
        return new ResponseEntity<>(secureService.getSecureKey(addr), HttpStatus.OK);
    }
}

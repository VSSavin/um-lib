package io.github.vssavin.umlib.controller;

import io.github.vssavin.umlib.utils.UmUtil;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by vssavin on 16.05.2022.
 */
@RestController
public class SecureController extends DefaultSecureController {

    public SecureController(UmUtil umUtil) {
        super(umUtil);
    }
}

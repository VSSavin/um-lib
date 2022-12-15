package io.github.vssavin.umlib.service.impl;

import io.github.vssavin.securelib.AESSecure;
import io.github.vssavin.umlib.service.SecureService;
import org.springframework.stereotype.Service;

/**
 * @author vssavin on 18.01.22
 */
@Service
public class AESSecureService extends AESSecure implements SecureService {
    @Override
    public String toString() {
        return "AES";
    }
}

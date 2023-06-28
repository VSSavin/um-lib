package io.github.vssavin.umlib.security;

import io.github.vssavin.securelib.RSASecure;
import io.github.vssavin.umlib.security.SecureService;
import org.springframework.stereotype.Component;

/**
 * @author vssavin on 24.01.22
 */
@Component
public class RSASecureService extends RSASecure implements SecureService {
    @Override
    public String toString() {
        return "RSA";
    }
}

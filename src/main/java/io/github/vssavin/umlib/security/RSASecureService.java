package io.github.vssavin.umlib.security;

import io.github.vssavin.securelib.RSASecure;
import io.github.vssavin.umlib.security.SecureService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @author vssavin on 24.01.22
 */
@Service
public class RSASecureService extends RSASecure implements SecureService {
    @Override
    public String toString() {
        return "RSA";
    }
}

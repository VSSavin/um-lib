package com.github.vssavin.umlib.security;

import io.github.vssavin.securelib.RSASecure;
import org.springframework.stereotype.Service;

/**
 * Provides a service that uses the RSA encryption algorithm.
 *
 * @author vssavin on 24.01.22
 */
@Service
class RSASecureService extends RSASecure implements SecureService {
    @Override
    public String toString() {
        return "RSA";
    }
}

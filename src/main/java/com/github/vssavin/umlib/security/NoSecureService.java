package com.github.vssavin.umlib.security;

import io.github.vssavin.securelib.NoSecure;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Created by vssavin on 17.05.2022.
 */
@Service
@Primary
class NoSecureService extends NoSecure implements SecureService {
    @Override
    public String toString() {
        return "no";
    }
}

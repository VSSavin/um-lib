package com.github.vssavin.umlib.domain.security;

import com.github.vssavin.jcrypt.JKeyStorage;
import com.github.vssavin.jcrypt.js.JavaJsJCryptRSA;
import com.github.vssavin.jcrypt.keystorage.RSAKeyStorage;
import org.springframework.stereotype.Service;

/**
 * Provides a service that uses the RSA encryption algorithm.
 *
 * @author vssavin on 24.01.22
 */
@Service
class RSASecureService extends JavaJsJCryptRSA implements SecureService {

    private final JKeyStorage keyStorage = new RSAKeyStorage();

    @Override
    public String toString() {
        return "RSA";
    }

    @Override
    public String getPublicKey() {
        return keyStorage.getPublicKey();
    }

    @Override
    public String getPublicKey(String id) {
        return keyStorage.getPublicKey(id);
    }

    @Override
    public String getPrivateKey() {
        return keyStorage.getPrivateKey();
    }

    @Override
    public String getPrivateKey(String id) {
        return keyStorage.getPrivateKey(id);
    }

}

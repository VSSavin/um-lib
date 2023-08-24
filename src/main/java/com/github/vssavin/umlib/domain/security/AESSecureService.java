package com.github.vssavin.umlib.domain.security;

import com.github.vssavin.jcrypt.JKeyStorage;
import com.github.vssavin.jcrypt.js.JsJCryptAES;
import com.github.vssavin.jcrypt.keystorage.AESKeyStorage;
import org.springframework.stereotype.Service;

/**
 * Provides a service that uses the AES encryption algorithm.
 *
 * @author vssavin on 18.01.22
 */
@Service
class AESSecureService extends JsJCryptAES implements SecureService {
    private final JKeyStorage keyStorage = new AESKeyStorage();

    @Override
    public String toString() {
        return "AES";
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

package com.github.vssavin.umlib.domain.security;

import com.github.vssavin.jcrypt.JCrypt;
import com.github.vssavin.jcrypt.JKeyStorage;
import com.github.vssavin.jcrypt.js.JsCryptCompatible;
import com.github.vssavin.jcrypt.js.JsJCryptEngine;
import com.github.vssavin.jcrypt.js.JsJCryptStub;

import java.util.List;

/**
 * Main interface for using various encryption algorithms.
 *
 * @author vssavin on 18.01.22
 */
public interface SecureService extends JCrypt, JKeyStorage, JsCryptCompatible {

    static SecureService defaultSecureService() {
        return new SecureService() {

            private final JsJCryptEngine engine = new JsJCryptStub();

            @Override
            public String getEncryptMethodName() {
                return engine.getEncryptMethodName();
            }

            @Override
            public String getDecryptMethodName() {
                return engine.getDecryptMethodName();
            }

            @Override
            public List<String> getScriptsList() {
                return engine.getScriptsList();
            }

            @Override
            public String getPublicKey() {
                return "";
            }

            @Override
            public String getPublicKey(String id) {
                return "";
            }

            @Override
            public String getPrivateKey() {
                return "";
            }

            @Override
            public String getPrivateKey(String id) {
                return "";
            }

            @Override
            public String encrypt(String message, String key) {
                return engine.encrypt(message, key);
            }

            @Override
            public String decrypt(String encrypted, String key) {
                return engine.decrypt(encrypted, key);
            }

            @Override
            public String encrypt(String message) {
                return engine.encrypt(message);
            }

            @Override
            public String decrypt(String encrypted) {
                return engine.decrypt(encrypted);
            }
        };
    }
}

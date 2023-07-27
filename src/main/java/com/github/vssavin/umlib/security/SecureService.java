package com.github.vssavin.umlib.security;

import io.github.vssavin.securelib.Secure;

/**
 * Main interface for using various encryption algorithms.
 *
 * @author vssavin on 18.01.22
 */
public interface SecureService extends Secure {
    static String getEncryptionMethodName(SecureAlgorithm secureAlgorithm) {
        return Secure.getEncryptionMethodName(secureAlgorithm);
    }

    static String getDecryptionMethodName(SecureAlgorithm secureAlgorithm) {
        return Secure.getDecryptionMethodName(secureAlgorithm);
    }

    static SecureService defaultSecureService() {
        return new SecureService() {
            @Override
            public String getSecureKey(String s) {
                return "";
            }

            @Override
            public String decrypt(String encoded, String key) {
                return encoded;
            }

            @Override
            public String encrypt(String message, String key) {
                return message;
            }

            @Override
            public String getEncryptMethodNameForView() {
                return Secure.getEncryptionMethodName(SecureAlgorithm.NOSECURE);
            }

            @Override
            public String toString() {
                return "no";
            }
        };
    }
}

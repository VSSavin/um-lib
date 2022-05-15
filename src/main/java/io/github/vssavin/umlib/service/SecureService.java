package io.github.vssavin.umlib.service;

import io.github.vssavin.securelib.Secure;

/**
 * @author vssavin on 18.01.22
 */
public interface SecureService extends Secure {
    static String getEncryptionMethodName(SecureAlgorithm secureAlgorithm) {
        return Secure.getEncryptionMethodName(secureAlgorithm);
    }

    static String getDecryptionMethodName(SecureAlgorithm secureAlgorithm) {
        return Secure.getDecryptionMethodName(secureAlgorithm);
    }
}

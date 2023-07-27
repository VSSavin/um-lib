package com.github.vssavin.umlib.security;

import io.github.vssavin.securelib.PlatformSpecificSecureImpl;
import org.springframework.stereotype.Service;

/**
 * Provides a service that uses a platform-specific (OS dependent) encryption algorithm.
 *
 * @author vssavin on 21.01.22
 */
@Service
class ApplicationSecureService extends PlatformSpecificSecureImpl {
}

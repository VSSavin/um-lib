package com.github.vssavin.umlib.domain.security;

import com.github.vssavin.jcrypt.osplatform.OSPlatformCrypt;
import org.springframework.stereotype.Service;

/**
 * Provides a service that uses a platform-specific (OS dependent) encryption algorithm.
 *
 * @author vssavin on 21.01.22
 */
@Service
class ApplicationSecureService extends OSPlatformCrypt {

}

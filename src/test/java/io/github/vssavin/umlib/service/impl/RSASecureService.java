package io.github.vssavin.umlib.service.impl;

import io.github.vssavin.securelib.RSASecure;
import io.github.vssavin.umlib.service.SecureService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * @author vssavin on 24.01.22
 */
@Component
@Primary
public class RSASecureService extends RSASecure implements SecureService {
}

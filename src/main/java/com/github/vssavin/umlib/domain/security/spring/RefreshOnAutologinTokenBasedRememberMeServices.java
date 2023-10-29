package com.github.vssavin.umlib.domain.security.spring;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Identifies previously remembered users by a Base-64 encoded cookie and refresh it on
 * autoLogin event.
 *
 * @author vssavin on 29.10.2023
 */
public class RefreshOnAutologinTokenBasedRememberMeServices extends TokenBasedRememberMeServices {

    public RefreshOnAutologinTokenBasedRememberMeServices(String key, UserDetailsService userDetailsService) {
        super(key, userDetailsService);
    }

    @Override
    protected UserDetails processAutoLoginCookie(String[] cookieTokens, HttpServletRequest request,
            HttpServletResponse response) {
        UserDetails result = super.processAutoLoginCookie(cookieTokens, request, response);

        int tokenLifetime = getTokenValiditySeconds();
        long expiryTime = System.currentTimeMillis();
        // SEC-949
        expiryTime += 1000L * ((tokenLifetime < 0) ? TWO_WEEKS_S : tokenLifetime);
        String signatureValue = makeTokenSignature(expiryTime, cookieTokens[0], result.getPassword());
        setCookie(new String[] { cookieTokens[0], Long.toString(expiryTime), cookieTokens[2], signatureValue },
                tokenLifetime, request, response);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Added remember-me cookie for user '" + cookieTokens[0] + "', expiry: '"
                    + new Date(expiryTime) + "'");
        }

        return result;
    }

}

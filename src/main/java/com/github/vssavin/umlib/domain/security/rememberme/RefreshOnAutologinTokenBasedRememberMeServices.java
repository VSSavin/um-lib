package com.github.vssavin.umlib.domain.security.rememberme;

import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Identifies previously remembered users by a Base-64 encoded cookie and refresh it on
 * autoLogin event. Also implements method {@link #retrieveAuthentication} to create
 * {@link Authentication} object without changing response cookies. It contains a
 * temporary local cache of usernames to reduce database connections.
 *
 * @author vssavin on 29.10.2023
 */
public class RefreshOnAutologinTokenBasedRememberMeServices extends TokenBasedRememberMeServices
        implements Authenticator {

    private final Map<String, UserDetails> userDetailsCache = new ConcurrentHashMap<>();

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();

    private int userRemoveDelaySeconds = 10;

    public RefreshOnAutologinTokenBasedRememberMeServices(String key, UserDetailsService userDetailsService) {
        super(key, userDetailsService);
    }

    @Override
    protected UserDetails processAutoLoginCookie(String[] cookieTokens, HttpServletRequest request,
            HttpServletResponse response) {
        this.logger.trace("Processing autoLogin cookie...");
        UserDetails result = getUserDetails(cookieTokens, request, response);

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

    @Override
    public Authentication retrieveAuthentication(HttpServletRequest request, HttpServletResponse response) {
        this.logger.trace("Retrieving authentication...");
        String rememberMeCookie = extractRememberMeCookie(request);
        if (rememberMeCookie == null || rememberMeCookie.isEmpty()) {
            return null;
        }

        try {
            String[] cookieTokens = decodeCookie(rememberMeCookie);
            UserDetails user = getUserDetails(cookieTokens, request, response);
            userDetailsChecker.check(user);
            this.logger.debug("Remember-me cookie accepted!");
            return createSuccessfulAuthentication(request, user);
        }
        catch (CookieTheftException ex) {
            throw ex;
        }
        catch (UsernameNotFoundException ex) {
            this.logger.debug("Remember-me login was valid but corresponding user not found.", ex);
        }
        catch (InvalidCookieException ex) {
            this.logger.debug("Invalid remember-me cookie: " + ex.getMessage());
        }
        catch (AccountStatusException ex) {
            this.logger.debug("Invalid UserDetails: " + ex.getMessage());
        }
        catch (RememberMeAuthenticationException ex) {
            this.logger.debug(ex.getMessage());
        }

        return null;
    }

    @Override
    public void setUserDetailsChecker(UserDetailsChecker userDetailsChecker) {
        super.setUserDetailsChecker(userDetailsChecker);
        this.userDetailsChecker = userDetailsChecker;
    }

    public void setUserRemoveDelaySeconds(int userRemoveDelaySeconds) {
        this.userRemoveDelaySeconds = userRemoveDelaySeconds;
    }

    public int getUserRemoveDelaySeconds() {
        return userRemoveDelaySeconds;
    }

    private UserDetails getUserDetails(String[] cookieTokens, HttpServletRequest request,
            HttpServletResponse response) {

        String userLogin = cookieTokens[0];
        String threadName = Thread.currentThread().getName();
        return userDetailsCache.computeIfAbsent(userLogin, login -> {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug(String.format("[%s] User '%s' not found in the local cache!", threadName, userLogin));
            }
            executorService.schedule(() -> {
                if (logger.isDebugEnabled()) {
                    this.logger
                        .debug(String.format("[%s] Removing user '%s' from th local cache!", threadName, userLogin));
                }
                userDetailsCache.remove(userLogin);
            }, userRemoveDelaySeconds, TimeUnit.SECONDS);
            return super.processAutoLoginCookie(cookieTokens, request, response);
        });
    }

}

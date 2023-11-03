package com.github.vssavin.umlib.domain.security.csrf;

import com.github.vssavin.umlib.domain.security.rememberme.Authenticator;
import com.github.vssavin.umlib.domain.security.rememberme.UserRememberMeToken;
import com.github.vssavin.umlib.domain.security.rememberme.UserRememberMeTokenRepository;
import com.github.vssavin.umlib.domain.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.util.Assert;
import org.thymeleaf.util.FastStringWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A CsrfTokenRepository that stores the CsrfToken in the user management database.
 *
 * @author vssavin on 30.10.2023
 */
public class UmCsrfTokenRepository implements CsrfTokenRepository {

    private static final Logger log = LoggerFactory.getLogger(UmCsrfTokenRepository.class);

    private static final String DEFAULT_CSRF_PARAMETER_NAME = "_csrf";

    private static final String DEFAULT_CSRF_HEADER_NAME = "X-CSRF-TOKEN";

    private static final int TWO_WEEKS_SECONDS = 1209600;

    private String parameterName = DEFAULT_CSRF_PARAMETER_NAME;

    private String headerName = DEFAULT_CSRF_HEADER_NAME;

    private CsrfToken anonymousDefaultToken = new DefaultCsrfToken(this.headerName, this.parameterName,
            createNewToken());

    private boolean useCache = false;

    private static final Map<Long, List<UserCsrfToken>> csrfCache = new ConcurrentHashMap<>(32);

    private static final Map<UserRememberMeToken, UserCsrfToken> rememberMeCsrfMap = new ConcurrentHashMap<>(32);

    private final Authenticator authenticator;

    private final UserCsrfTokenRepository tokenRepository;

    private final UserRememberMeTokenRepository rememberMeTokenRepository;

    private int tokenValiditySeconds = TWO_WEEKS_SECONDS;

    public UmCsrfTokenRepository(Authenticator authenticator, UserCsrfTokenRepository tokenRepository,
            UserRememberMeTokenRepository rememberMeTokenRepository) {
        this.authenticator = authenticator;
        this.tokenRepository = tokenRepository;
        this.rememberMeTokenRepository = rememberMeTokenRepository;
    }

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        return new DefaultCsrfToken(this.headerName, this.parameterName, createNewToken());
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = authenticator.retrieveAuthentication(request, response);

        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            log.debug("Requested saving token for user {}", user);
            List<UserRememberMeToken> rememberMeTokens;

            rememberMeTokens = rememberMeTokenRepository.findByUserId(user.getId());

            AtomicReference<UserRememberMeToken> requestRememberMeToken = new AtomicReference<>();
            rememberMeTokens.forEach(rememberMeToken -> {
                if (isRememberMeTokenPresentInCookies(request, rememberMeToken)) {
                    requestRememberMeToken.set(rememberMeToken);
                }
            });

            if (token == null && requestRememberMeToken.get() != null) {
                // delete token from storage by user remember-me token
                deleteTokenFromStorage(user, requestRememberMeToken.get());
            }
            else {
                if (token != null && !token.getToken().equals(anonymousDefaultToken.getToken())) {
                    // save token to database by user id
                    saveTokenToStorage(user, requestRememberMeToken.get(), token);
                }
            }
        }
    }

    private void deleteTokenFromStorage(User user, UserRememberMeToken rememberMeToken) {
        log.debug("Requested token deleting {} {}", user, rememberMeToken);
        UserCsrfToken requestedCsrfToken = rememberMeCsrfMap.get(rememberMeToken);
        if (!rememberMeToken.getToken().isEmpty()) {

            if (useCache) {
                List<UserCsrfToken> userCsrfTokens = csrfCache.get(user.getId());
                if (userCsrfTokens != null) {
                    userCsrfTokens.remove(requestedCsrfToken);
                }
            }
            else {
                log.debug("Deleting csrf token from the database!");
                tokenRepository.deleteByToken(requestedCsrfToken.getToken());
            }
            rememberMeCsrfMap.remove(rememberMeToken);
        }
        log.debug("Deleting finished!");
    }

    private void saveTokenToStorage(User user, UserRememberMeToken rememberMeToken, CsrfToken token) {
        log.debug("Requested token saving {} {} {}", user, rememberMeToken, token);
        List<UserCsrfToken> userTokens;
        Optional<UserCsrfToken> optionalUserCsrfToken;
        UserCsrfToken userCsrfToken;
        if (useCache) {
            userTokens = csrfCache.get(user.getId());
            if (userTokens == null) {
                userTokens = Collections.emptyList();
            }
        }
        else {
            log.debug("Searching in database!");
            userTokens = tokenRepository.findByUserId(user.getId());
        }

        optionalUserCsrfToken = userTokens.stream()
            .filter(userToken -> userToken.getToken().equals(token.getToken()))
            .findFirst();
        userCsrfToken = optionalUserCsrfToken.orElseGet(() -> new UserCsrfToken(user.getId(), token.getToken(),
                new Date(System.currentTimeMillis() + (long) tokenValiditySeconds * 1000)));

        if (useCache) {
            csrfCache.put(user.getId(), Collections.singletonList(userCsrfToken));
        }
        else {
            log.debug("Saving data to the database!");
            tokenRepository.save(userCsrfToken);
        }

        if (rememberMeToken != null) {
            rememberMeCsrfMap.put(rememberMeToken, userCsrfToken);
        }

        log.debug("Token saving finished!");
    }

    public void setTokenValiditySeconds(int tokenValiditySeconds) {
        this.tokenValiditySeconds = tokenValiditySeconds;
    }

    public int getTokenValiditySeconds() {
        return tokenValiditySeconds;
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        log.debug("Loading token!");
        Authentication authentication = authenticator.retrieveAuthentication(request, new UmMockHttpServletResponse());

        if (authentication == null) {
            return anonymousDefaultToken;
        }

        if (authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            List<UserCsrfToken> userTokens;
            List<UserCsrfToken> tokensToUpdate = new ArrayList<>();
            // load token from database by user id or remember-me token
            if (useCache) {
                userTokens = csrfCache.get(user.getId());
                if (userTokens == null) {
                    userTokens = Collections.emptyList();
                }
            }
            else {
                log.debug("Searching token in the database for user {}", user);
                userTokens = tokenRepository.findByUserId(user.getId());
            }

            userTokens.forEach(token -> {
                if (token.getExpirationDate().getTime() < System.currentTimeMillis()) {
                    token.setExpirationDate(new Date(System.currentTimeMillis() + (long) tokenValiditySeconds * 1000));
                    tokensToUpdate.add(token);
                }
            });

            if (!useCache) {
                log.debug("Updating tokens in the database!");
                tokenRepository.saveAll(tokensToUpdate);
            }

            log.debug("Loading finished!");
            if (!userTokens.isEmpty()) {

                return new DefaultCsrfToken(this.headerName, this.parameterName, userTokens.get(0).getToken());
            }
        }

        return null;
    }

    private String createNewToken() {
        return UUID.randomUUID().toString();
    }

    private boolean isRememberMeTokenPresentInCookies(HttpServletRequest request, UserRememberMeToken rememberMeToken) {
        boolean present = false;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getValue().equals(rememberMeToken.getToken())) {
                present = true;
                break;
            }
        }
        return present;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    /**
     * Sets the {@link HttpServletRequest} parameter name that the {@link CsrfToken} is
     * expected to appear on
     * @param parameterName the new parameter name to use
     */
    public void setParameterName(String parameterName) {
        Assert.hasLength(parameterName, "parameterName cannot be null or empty");
        this.parameterName = parameterName;
        anonymousDefaultToken = new DefaultCsrfToken(this.headerName, this.parameterName, createNewToken());
    }

    /**
     * Sets the header name that the {@link CsrfToken} is expected to appear on and the
     * header that the response will contain the {@link CsrfToken}.
     * @param headerName the new header name to use
     */
    public void setHeaderName(String headerName) {
        Assert.hasLength(headerName, "headerName cannot be null or empty");
        this.headerName = headerName;
        anonymousDefaultToken = new DefaultCsrfToken(this.headerName, this.parameterName, createNewToken());
    }

    /**
     * Mock implementation of the HttpServletResponse interface.
     */
    private static class UmMockHttpServletResponse implements HttpServletResponse {

        @Override
        public void addCookie(Cookie cookie) {
            // Do nothing because this is a mock implementation
        }

        @Override
        public boolean containsHeader(String name) {
            return false;
        }

        @Override
        public String encodeURL(String url) {
            return url;
        }

        @Override
        public String encodeRedirectURL(String url) {
            return url;
        }

        @Override
        public String encodeUrl(String url) {
            return url;
        }

        @Override
        public String encodeRedirectUrl(String url) {
            return url;
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            // Do nothing because this is a mock implementation
        }

        @Override
        public void sendError(int sc) throws IOException {
            // Do nothing because this is a mock implementation
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            // Do nothing because this is a mock implementation
        }

        @Override
        public void setDateHeader(String name, long date) {
            // Do nothing because this is a mock implementation
        }

        @Override
        public void addDateHeader(String name, long date) {
            // Do nothing because this is a mock implementation
        }

        @Override
        public void setHeader(String name, String value) {
            // Do nothing because this is a mock implementation
        }

        @Override
        public void addHeader(String name, String value) {
            // Do nothing because this is a mock implementation
        }

        @Override
        public void setIntHeader(String name, int value) {
            // Do nothing because this is a mock implementation
        }

        @Override
        public void addIntHeader(String name, int value) {
            // Do nothing because this is a mock implementation
        }

        @Override
        public void setStatus(int sc) {
            // Do nothing because this is a mock implementation
        }

        @Override
        public void setStatus(int sc, String sm) {
            // Do nothing because this is a mock implementation
        }

        @Override
        public int getStatus() {
            return 0;
        }

        @Override
        public String getHeader(String name) {
            return "";
        }

        @Override
        public Collection<String> getHeaders(String name) {
            return Collections.emptyList();
        }

        @Override
        public Collection<String> getHeaderNames() {
            return Collections.emptyList();
        }

        @Override
        public String getCharacterEncoding() {
            return "";
        }

        @Override
        public String getContentType() {
            return "";
        }

        @Override
        public ServletOutputStream getOutputStream() {
            return null;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return new PrintWriter(new FastStringWriter());
        }

        @Override
        public void setCharacterEncoding(String charset) {
            // Do nothing because this is a mock implementation
        }

        @Override
        public void setContentLength(int len) {
            // Do nothing because this is a mock implementation
        }

        @Override
        public void setContentLengthLong(long len) {
            // Do nothing because this is a mock implementation
        }

        @Override
        public void setContentType(String type) {
            // Do nothing because this is a mock implementation
        }

        @Override
        public void setBufferSize(int size) {
            // Do nothing because this is a mock implementation
        }

        @Override
        public int getBufferSize() {
            return 0;
        }

        @Override
        public void flushBuffer() {
            // Do nothing because this is a mock implementation
        }

        @Override
        public void resetBuffer() {
            // Do nothing because this is a mock implementation
        }

        @Override
        public boolean isCommitted() {
            return false;
        }

        @Override
        public void reset() {
            // Do nothing because this is a mock implementation
        }

        @Override
        public void setLocale(Locale loc) {
            // Do nothing because this is a mock implementation
        }

        @Override
        public Locale getLocale() {
            return Locale.getDefault();
        }

    }

}

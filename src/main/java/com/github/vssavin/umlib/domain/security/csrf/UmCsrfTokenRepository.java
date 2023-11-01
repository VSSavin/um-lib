package com.github.vssavin.umlib.domain.security.csrf;

import com.github.vssavin.umlib.domain.security.rememberme.Authenticator;
import com.github.vssavin.umlib.domain.security.rememberme.UserRememberMeToken;
import com.github.vssavin.umlib.domain.security.rememberme.UserRememberMeTokenRepository;
import com.github.vssavin.umlib.domain.user.User;
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
import java.util.concurrent.atomic.AtomicReference;

/**
 * A CsrfTokenRepository that stores the CsrfToken in the user management database.
 *
 * @author vssavin on 30.10.2023
 */
public class UmCsrfTokenRepository implements CsrfTokenRepository {

    private static final String DEFAULT_CSRF_PARAMETER_NAME = "_csrf";

    private static final String DEFAULT_CSRF_HEADER_NAME = "X-CSRF-TOKEN";

    private static final int TWO_WEEKS_SECONDS = 1209600;

    private String parameterName = DEFAULT_CSRF_PARAMETER_NAME;

    private String headerName = DEFAULT_CSRF_HEADER_NAME;

    private CsrfToken anonymousDefaultToken = new DefaultCsrfToken(this.headerName, this.parameterName,
            createNewToken());

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
        Authentication authentication;
        if (token == null) {
            authentication = authenticator.retrieveAuthentication(request, response);
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                List<UserRememberMeToken> rememberMeTokens = rememberMeTokenRepository.findByUserId(user.getId());
                AtomicReference<String> requestRememberMeToken = new AtomicReference<>("");
                rememberMeTokens.forEach(rememberMeToken -> {
                    if (isRememberMeTokenPresentInCookies(request, rememberMeToken)) {
                        requestRememberMeToken.set(rememberMeToken.getToken());
                    }
                });

                // delete token from database by user remember-me token
                if (!requestRememberMeToken.get().isEmpty()) {
                    tokenRepository.deleteByToken(requestRememberMeToken.get());
                }
            }
        }
        else {
            authentication = authenticator.retrieveAuthentication(request, response);
            if (!token.getToken().equals(anonymousDefaultToken.getToken()) && authentication != null
                    && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                // save token to database by user id
                List<UserCsrfToken> userTokens = tokenRepository.findByUserId(user.getId());
                Optional<UserCsrfToken> optionalUserCsrfToken = userTokens.stream()
                    .filter(userToken -> userToken.getToken().equals(token.getToken()))
                    .findFirst();
                UserCsrfToken userCsrfToken = optionalUserCsrfToken.orElseGet(() -> new UserCsrfToken(user.getId(),
                        token.getToken(), new Date(System.currentTimeMillis() + (long) tokenValiditySeconds * 1000)));
                tokenRepository.save(userCsrfToken);
            }
        }
    }

    public void setTokenValiditySeconds(int tokenValiditySeconds) {
        this.tokenValiditySeconds = tokenValiditySeconds;
    }

    public int getTokenValiditySeconds() {
        return tokenValiditySeconds;
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        Authentication authentication = authenticator.retrieveAuthentication(request, new UmMockHttpServletResponse());

        if (authentication == null) {
            return anonymousDefaultToken;
        }
        if (authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            // load token from database by user id or remember-me token
            List<UserCsrfToken> userTokens = tokenRepository.findByUserId(user.getId());
            List<UserCsrfToken> tokensToUpdate = new ArrayList<>();
            userTokens.forEach(token -> {
                if (token.getExpirationDate().getTime() < System.currentTimeMillis()) {
                    token.setExpirationDate(new Date(System.currentTimeMillis() + (long) tokenValiditySeconds * 1000));
                    tokensToUpdate.add(token);
                }
            });
            tokenRepository.saveAll(tokensToUpdate);
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

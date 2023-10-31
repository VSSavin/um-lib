package com.github.vssavin.umlib.domain.security.rememberme;

import com.github.vssavin.umlib.domain.security.spring.RefreshOnAutologinTokenBasedRememberMeServices;
import com.github.vssavin.umlib.domain.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

/**
 * Identifies previously remembered users by a Base-64 encoded cookie, refresh it on
 * autoLogin event and stores it in the user management database.
 *
 * @author vssavin on 30.10.2023
 */
public class RefreshOnLoginDatabaseTokenBasedRememberMeService extends RefreshOnAutologinTokenBasedRememberMeServices {

    private final UserRememberMeTokenRepository tokenRepository;

    public RefreshOnLoginDatabaseTokenBasedRememberMeService(String key, UserDetailsService userDetailsService,
            UserRememberMeTokenRepository tokenRepository) {
        super(key, userDetailsService);
        this.tokenRepository = tokenRepository;
    }

    @Override
    protected UserDetails processAutoLoginCookie(String[] cookieTokens, HttpServletRequest request,
            HttpServletResponse response) {
        UserDetails result = super.processAutoLoginCookie(cookieTokens, request, response);
        saveRememberMeToken(request, result);
        return result;
    }

    @Override
    public void onLoginSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication successfulAuthentication) {
        super.onLoginSuccess(request, response, successfulAuthentication);
        if (successfulAuthentication.getPrincipal() instanceof User) {
            UserDetails userDetails = getUserDetailsService()
                .loadUserByUsername(((User) successfulAuthentication.getPrincipal()).getLogin());
            saveRememberMeToken(request, userDetails);
        }
    }

    private void saveRememberMeToken(HttpServletRequest request, UserDetails userDetails) {
        Authentication successfulAuthentication = createSuccessfulAuthentication(request, userDetails);
        if (successfulAuthentication.getPrincipal() instanceof User) {
            User user = (User) successfulAuthentication.getPrincipal();
            String rememberMeToken = extractRememberMeCookie(request);
            List<UserRememberMeToken> userTokens = tokenRepository.findByUserId(user.getId());
            Optional<UserRememberMeToken> optionalToken = userTokens.stream()
                .filter(userToken -> userToken.getToken().equals(rememberMeToken))
                .findFirst();
            UserRememberMeToken token;
            token = optionalToken.orElseGet(() -> new UserRememberMeToken(user.getId(), rememberMeToken));
            tokenRepository.save(token);
        }
    }

}

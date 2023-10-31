package com.github.vssavin.umlib.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * @author vssavin on 31.10.2023
 */
@Component
public class DefaultAuthConfig {

    private final AuthenticationSuccessHandler authSuccessHandler;

    private final AuthenticationFailureHandler authFailureHandler;

    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;

    private final LogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    public DefaultAuthConfig(AuthenticationSuccessHandler customAuthenticationSuccessHandler,
            AuthenticationFailureHandler customAuthenticationFailureHandler,
            OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService,
            LogoutSuccessHandler customLogoutSuccessHandler) {
        this.authSuccessHandler = customAuthenticationSuccessHandler;
        this.authFailureHandler = customAuthenticationFailureHandler;
        this.oAuth2UserService = customOAuth2UserService;
        this.logoutSuccessHandler = customLogoutSuccessHandler;
    }

    public AuthenticationSuccessHandler getAuthSuccessHandler() {
        return authSuccessHandler;
    }

    public AuthenticationFailureHandler getAuthFailureHandler() {
        return authFailureHandler;
    }

    public OAuth2UserService<OAuth2UserRequest, OAuth2User> getOAuth2UserService() {
        return oAuth2UserService;
    }

    public LogoutSuccessHandler getLogoutSuccessHandler() {
        return logoutSuccessHandler;
    }

}

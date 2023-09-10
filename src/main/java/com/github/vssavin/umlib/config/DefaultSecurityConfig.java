package com.github.vssavin.umlib.config;

import com.github.vssavin.umlib.domain.security.spring.BlackListFilter;
import com.github.vssavin.umlib.domain.security.spring.CustomOAuth2UserService;
import com.github.vssavin.umlib.domain.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.*;

/**
 * Provides default user management configuration for spring-security.
 *
 * @author vssavin on 17.05.2022.
 */
public class DefaultSecurityConfig {
    private final UserService userService;
    private final AuthenticationSuccessHandler authSuccessHandler;
    private final AuthenticationFailureHandler authFailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final LogoutSuccessHandler logoutSuccessHandler;
    private final OAuth2Config oAuth2Config;

    private final UmConfigurer configurer;

    @Autowired
    public DefaultSecurityConfig(UmConfigurer configurer, UserService userService,
                                 AuthenticationSuccessHandler customAuthenticationSuccessHandler,
                                 AuthenticationFailureHandler customAuthenticationFailureHandler,
                                 CustomOAuth2UserService customOAuth2UserService,
                                 LogoutSuccessHandler customLogoutSuccessHandler,
                                 OAuth2Config oAuth2Config) {
        this.configurer = configurer;
        this.userService = userService;
        this.authSuccessHandler = customAuthenticationSuccessHandler;
        this.authFailureHandler = customAuthenticationFailureHandler;
        this.customOAuth2UserService = customOAuth2UserService;
        this.logoutSuccessHandler = customLogoutSuccessHandler;
        this.oAuth2Config = oAuth2Config;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity httpSecurity, PasswordEncoder passwordEncoder,
                                                       AuthenticationProvider customAuthenticationProvider)
            throws Exception {

        httpSecurity.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userService)
                .passwordEncoder(passwordEncoder);
        return httpSecurity.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(customAuthenticationProvider)
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return userService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity, UmConfig umConfig, BlackListFilter blackListFilter)
            throws Exception {
        httpSecurity.addFilterBefore(blackListFilter, BasicAuthenticationFilter.class);

        httpSecurity.sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.ALWAYS));

        List<AuthorizedUrlPermission> urlPermissions = umConfig.getAuthorizedUrlPermissions();

        registerUrls(httpSecurity, urlPermissions);

        if (!umConfig.isCsrfEnabled()) {
            httpSecurity.csrf(AbstractHttpConfigurer::disable);
        }

        httpSecurity
                .formLogin(customizer -> customizer
                        .failureHandler(authFailureHandler)
                        .successHandler(authSuccessHandler)
                        .loginPage(configurer.getLoginUrl())
                        .loginProcessingUrl(configurer.getLoginProcessingUrl())
                        .usernameParameter("username")
                        .passwordParameter("password")
                )
                .logout(customizer -> customizer.permitAll()
                        .logoutUrl(configurer.getLogoutUrl())
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                );

        if (!Objects.equals(oAuth2Config.getGoogleClientId(), "")) {
            httpSecurity.oauth2Login(customizer -> customizer
                    .successHandler(authSuccessHandler)
                    .failureHandler(authFailureHandler)
                    .loginPage(configurer.getLoginUrl())
                    .userInfoEndpoint(userInfoEndpointConfig ->
                            userInfoEndpointConfig.userService(customOAuth2UserService))
                    );
        }

        return httpSecurity.build();
    }

    private void registerUrls(HttpSecurity httpSecurity, List<AuthorizedUrlPermission> urlPermissions) throws Exception {

        List<AuthorizedUrlPermission> permissions = new ArrayList<>(urlPermissions);
        permissions.sort(Comparator.comparingInt(o -> o.getRoles().length));

        for (AuthorizedUrlPermission urlPermission : permissions) {
            String[] roles = urlPermission.getRoles();
            httpSecurity.authorizeHttpRequests(customizer -> registerUrl(customizer, roles, urlPermission));
        }
    }

    private void registerUrl(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry,
            String[] roles, AuthorizedUrlPermission urlPermission) {
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl authorizedUrl = registry
                .requestMatchers(new AntPathRequestMatcher(urlPermission.getUrl(), urlPermission.getHttpMethod()));
        if (roles != null && roles.length == 0) {
            authorizedUrl.permitAll();
        } else if (roles != null) {
            authorizedUrl.hasAnyRole(urlPermission.getRoles());
        }
    }
}

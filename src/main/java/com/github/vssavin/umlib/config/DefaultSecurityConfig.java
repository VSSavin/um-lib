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

import java.util.List;
import java.util.Objects;

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
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder,
                                                       AuthenticationProvider customAuthenticationProvider)
            throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userService)
                .passwordEncoder(passwordEncoder)
                .and()
                .authenticationProvider(customAuthenticationProvider)
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return userService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UmConfig umConfig, BlackListFilter blackListFilter)
            throws Exception {
        http.addFilterBefore(blackListFilter, BasicAuthenticationFilter.class);

        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS);

        List<AuthorizedUrlPermission> urlPermissions = umConfig.getAuthorizedUrlPermissions();

        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry =
                registerUrls(http, urlPermissions);

        registry.and()
                .formLogin().failureHandler(authFailureHandler)
                .successHandler(authSuccessHandler)
                .loginPage(configurer.getLoginUrl())
                .loginProcessingUrl(configurer.getLoginProcessingUrl())
                .usernameParameter("username")
                .passwordParameter("password")
                .and()
                .logout()
                .permitAll()
                .logoutUrl(configurer.getLogoutUrl())
                .logoutSuccessHandler(logoutSuccessHandler)
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true);

        if (!Objects.equals(oAuth2Config.getGoogleClientId(), "")) {
            registry.and()
                    .oauth2Login()
                    .successHandler(authSuccessHandler)
                    .failureHandler(authFailureHandler)
                    .loginPage(configurer.getLoginUrl())
                    .userInfoEndpoint()
                    .userService(customOAuth2UserService);
        }

        return http.build();
    }

    private AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registerUrls(
            HttpSecurity http, List<AuthorizedUrlPermission> urlPermissions) throws Exception {

        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry =
                http.authorizeHttpRequests();

        for (AuthorizedUrlPermission urlPermission : urlPermissions) {
            String[] roles = urlPermission.getRoles();
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl authorizedUrl =
                    registry.requestMatchers(new AntPathRequestMatcher(urlPermission.getUrl(), null));

            if (roles != null && roles.length == 0) {
                registry = authorizedUrl.permitAll();
            } else if (roles != null) {
                registry = authorizedUrl.hasAnyRole(urlPermission.getRoles());
            }
        }

        return registry;
    }
}

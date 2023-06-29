package com.github.vssavin.umlib.config;

import com.github.vssavin.umlib.security.spring.BannedIpFilter;
import com.github.vssavin.umlib.security.spring.CustomOAuth2UserService;
import com.github.vssavin.umlib.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
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
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created by vssavin on 17.05.2022.
 */
public class DefaultSecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(DefaultSecurityConfig.class);

    private final BeanFactory beanFactory;
    private final UserService userService;
    private final AuthenticationSuccessHandler authSuccessHandler;
    private final AuthenticationFailureHandler authFailureHandler;
    private final AuthenticationProvider authProvider;
    private final LogoutHandler logoutHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final LogoutSuccessHandler logoutSuccessHandler;
    private final PasswordEncoder passwordEncoder;
    private final OAuth2Config oAuth2Config;

    private UmConfigurer configurer;

    @Autowired
    public DefaultSecurityConfig(UmConfig umConfig, BeanFactory beanFactory, UserService userService,
                                 AuthenticationSuccessHandler customAuthenticationSuccessHandler,
                                 AuthenticationFailureHandler customAuthenticationFailureHandler,
                                 AuthenticationProvider customAuthenticationProvider,
                                 LogoutHandler customLogoutHandler, CustomOAuth2UserService customOAuth2UserService,
                                 LogoutSuccessHandler customLogoutSuccessHandler, PasswordEncoder passwordEncoder,
                                 OAuth2Config oAuth2Config) {
        this.beanFactory = beanFactory;
        this.userService = userService;
        this.authSuccessHandler = customAuthenticationSuccessHandler;
        this.authFailureHandler = customAuthenticationFailureHandler;
        this.authProvider = customAuthenticationProvider;
        this.logoutHandler = customLogoutHandler;
        this.customOAuth2UserService = customOAuth2UserService;
        this.logoutSuccessHandler = customLogoutSuccessHandler;
        this.passwordEncoder = passwordEncoder;
        this.oAuth2Config = oAuth2Config;
        umConfig.updateAuthorizedPermissions();
    }

    @PostConstruct
    public void init() {
        try {
            configurer = beanFactory.getBean(UmConfigurer.class);
            UmConfig.adminSuccessUrl = configurer.getAdminSuccessUrl();
            UmConfig.successUrl = configurer.getSuccessUrl();
        } catch(NoSuchBeanDefinitionException e) {
            log.warn("User management configurer (UmConfigurer bean) not found! Using default configurer!");
            configurer = new UmConfigurer();
            UmConfig.adminSuccessUrl = configurer.getAdminSuccessUrl();
            UmConfig.successUrl = configurer.getSuccessUrl();
        }
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, UserDetailsService userDetailsService)
            throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder)
                .and()
                .authenticationProvider(authProvider)
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return userService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.addFilterBefore(
                new BannedIpFilter(), BasicAuthenticationFilter.class);

        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS);

        List<AuthorizedUrlPermission> urlPermissions = UmConfig.getAuthorizedUrlPermissions();

        urlPermissions.add(new AuthorizedUrlPermission("/oauth/**", new String[]{}));
        urlPermissions.add(new AuthorizedUrlPermission("/login/oauth2/code/google", new String[]{}));

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
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler(logoutSuccessHandler)
                .deleteCookies("JSESSIONID");

        if (!"".equals(oAuth2Config.getGoogleClientId())) {
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

        for(AuthorizedUrlPermission urlPermission : urlPermissions) {
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

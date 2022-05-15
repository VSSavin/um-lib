package io.github.vssavin.umlib.config;

import io.github.vssavin.umlib.security.spring.BannedIpFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.sql.DataSource;

/**
 * @author vssavin on 18.12.2021
 */
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String ADMIN_SUCCESS_URL = "/admin";
    public static final String SUCCESS_URL = "/games/index.html";
    public static final String LOGIN_URL = "/login";
    public static final String LOGIN_PROCESSING_URL = "/perform-login";
    public static final String LOGOUT_URL = "/logout";

    private DataSource dataSource;
    private AuthenticationSuccessHandler authSuccessHandler;
    private AuthenticationFailureHandler authFailureHandler;
    private AuthenticationProvider authProvider;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(DataSource dataSource, AuthenticationSuccessHandler authSuccessHandler,
                          AuthenticationFailureHandler authFailureHandler, AuthenticationProvider authProvider,
                          PasswordEncoder passwordEncoder) {
        this.dataSource = dataSource;
        this.authSuccessHandler = authSuccessHandler;
        this.authFailureHandler = authFailureHandler;
        this.authProvider = authProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider);
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(passwordEncoder)
                .usersByUsernameQuery(
                        "select login, password, 'true' from users " +
                                "where login=?")
                .authoritiesByUsernameQuery(
                        "select login, authority from users " +
                                "where login=?");
    }

    @Bean
    public JdbcUserDetailsManager jdbcUserDetailsManager() throws Exception {
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager();
        jdbcUserDetailsManager.setDataSource(dataSource);
        return jdbcUserDetailsManager;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.addFilterBefore(
                new BannedIpFilter(), BasicAuthenticationFilter.class);

        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS);

        http.authorizeRequests()
                .antMatchers("/js/**", "/css/**").permitAll()
                .antMatchers("/user/registration").permitAll()
                .antMatchers("/user/perform-register").permitAll()
                .antMatchers("/user/confirmUser").permitAll()
                .antMatchers("/admin**").hasRole( "ADMIN")
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/user/**").hasAnyRole("ADMIN", "USER")
                .antMatchers("/games/**").authenticated()
                .and()
                .formLogin().failureHandler(authFailureHandler)
                .successHandler(authSuccessHandler)
                .loginPage(LOGIN_URL)
                .loginProcessingUrl(LOGIN_PROCESSING_URL)
                .usernameParameter("username")
                .passwordParameter("password")
                //.defaultSuccessUrl(SUCCESS_URL)
                .and()
                .logout()
                .logoutUrl(LOGOUT_URL)
                .deleteCookies("JSESSIONID");
    }

}

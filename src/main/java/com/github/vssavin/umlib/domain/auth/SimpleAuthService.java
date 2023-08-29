package com.github.vssavin.umlib.domain.auth;

import com.github.vssavin.umlib.config.UmConfig;
import com.github.vssavin.umlib.domain.event.EventService;
import com.github.vssavin.umlib.domain.event.EventType;
import com.github.vssavin.umlib.domain.security.SecureService;
import com.github.vssavin.umlib.domain.security.spring.CustomUsernamePasswordAuthenticationToken;
import com.github.vssavin.umlib.domain.user.User;
import com.github.vssavin.umlib.domain.user.UserExpiredException;
import com.github.vssavin.umlib.domain.user.UserNotFoundException;
import com.github.vssavin.umlib.domain.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author vssavin on 29.08.2023
 */
@Service
public class SimpleAuthService implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(SimpleAuthService.class);
    private static final Class<? extends Authentication> authenticationClass = CustomUsernamePasswordAuthenticationToken.class;

    private static final ConcurrentHashMap<String, Integer> blackList = new ConcurrentHashMap<>(50);
    private static final ConcurrentHashMap<String, Long> banExpireTimes = new ConcurrentHashMap<>(50);

    private static final int MAX_FAILURE_COUNTS = 3;
    private static final int BLOCKING_TIME_MINUTES = 60;

    private final UserService userService;
    private final EventService eventService;
    private final SecureService secureService;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public SimpleAuthService(UserService userService, EventService eventService, UmConfig umConfig, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.eventService = eventService;
        this.secureService = umConfig.getAuthService();
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        Object credentials = authentication.getCredentials();
        Object userName = authentication.getPrincipal();
        if (credentials != null) {
            UserDetails user = userService.loadUserByUsername(userName.toString());
            if (user != null) {
                checkUserDetails(user);

                Object details = authentication.getDetails();
                String addr = "";
                if (details instanceof WebAuthenticationDetails) {
                    addr = ((WebAuthenticationDetails) details).getRemoteAddress();
                }

                String password = secureService.decrypt(credentials.toString(), secureService.getPrivateKey(addr));
                if (passwordEncoder.matches(password, user.getPassword())) {
                    List<GrantedAuthority> authorities = new ArrayList<>(user.getAuthorities());
                    return new CustomUsernamePasswordAuthenticationToken(authentication.getPrincipal(),
                            password, authorities);
                } else {
                    throw new BadCredentialsException("Authentication failed");
                }

            } else {
                return authentication;
            }

        } else {
            return authentication;
        }
    }

    @Override
    @Transactional
    public Collection<GrantedAuthority> processSuccessAuthentication(Authentication authentication, HttpServletRequest request, EventType eventType) {
        User user = null;
        try {
            OAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            user = userService.processOAuthPostLogin(oAuth2User);
        } catch (ClassCastException e) {
            //ignore, it's ok
        }

        if (user == null) {
            user = userService.getUserByLogin(authentication.getPrincipal().toString());
        }

        if (user == null) {
            throw new UserNotFoundException(
                    String.format("User [%s] not found!", authentication.getPrincipal().toString()));
        }

        if (user.getExpirationDate().before(new Date())) {
            userService.deleteUser(user);
            throw new UserExpiredException(String.format("User [%s] has been expired!", user.getLogin()));
        }

        saveUserEvent(user, request, eventType);
        return user.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthority())).collect(Collectors.toList());
    }

    @Override
    public boolean authenticationAllowed(String ipAddress) {
        Integer failureCounts = blackList.get(ipAddress);
        if (failureCounts == null) {
            return true;
        } else {
            if (failureCounts >= MAX_FAILURE_COUNTS) {
                Long expireTime = banExpireTimes.get(ipAddress);
                if (expireTime != null && (Calendar.getInstance().getTimeInMillis() > expireTime)) {
                    expireTime = 0L;
                    banExpireTimes.put(ipAddress, expireTime);
                    failureCounts = 0;
                    blackList.put(ipAddress, failureCounts);
                    return true;
                }
                return false;
            } else {
                return true;
            }
        }
    }

    @Override
    public void processFailureAuthentication(HttpServletRequest request, HttpServletResponse response,
                                             AuthenticationException exception) {
        String userIp = request.getRemoteAddr();
        Integer failureCounts = blackList.get(userIp);

        if (failureCounts == null) {
            blackList.put(userIp, 1);
        } else {
            if (failureCounts < MAX_FAILURE_COUNTS) {
                failureCounts++;
                blackList.put(userIp, failureCounts);
                if (failureCounts >= MAX_FAILURE_COUNTS) {
                    banExpireTimes.put(userIp,
                            Calendar.getInstance().getTimeInMillis() + (BLOCKING_TIME_MINUTES * 60 * 1000));
                    log.info("IP {} has ben blocked!", userIp);
                    throw new AuthenticationForbiddenException("Sorry! You has been blocked! Try again later!");
                }
            }
        }
    }

    @Override
    public Class<? extends Authentication> authenticationClass() {
        return authenticationClass;
    }

    private void saveUserEvent(User user, HttpServletRequest request,
                                   EventType eventType) {
        String message = "";
        switch (eventType) {
            case LOGGED_IN:
                message = String.format("User [%s] logged in using IP: %s", user.getLogin(), request.getRemoteAddr());
                break;
            case LOGGED_OUT:
                message = String.format("User [%s] logged out using IP: %s", user.getLogin(), request.getRemoteAddr());
                break;
        }
        eventService.createEvent(user, eventType, message);
    }

    private void checkUserDetails(UserDetails userDetails) {
        if (!userDetails.isAccountNonExpired()) {
            throw new AccountExpiredException("Account is expired!");
        }
        if (!userDetails.isAccountNonLocked()) {
            throw new LockedException("Account is locked!");
        }
        if (!userDetails.isEnabled()) {
            throw new DisabledException("Account is disabled!");
        }
    }
}

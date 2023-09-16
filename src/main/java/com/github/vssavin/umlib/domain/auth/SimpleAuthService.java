package com.github.vssavin.umlib.domain.auth;

import com.github.vssavin.umlib.config.UmConfig;
import com.github.vssavin.umlib.config.aspect.UmRouteDatasource;
import com.github.vssavin.umlib.domain.event.EventService;
import com.github.vssavin.umlib.domain.event.EventType;
import com.github.vssavin.umlib.domain.security.SecureService;
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
 * An {@link com.github.vssavin.umlib.domain.auth.AuthService} implementation that: 1.
 * Attempts to authenticate the user using o2Auth or user/password mechanism. 2. Handling
 * success authentication and creating the corresponding user event. 3. Handling failed
 * authentication adds the current IP address to the blacklist. 4. Checks if
 * authentication is allowed for the specified ip address uses a blacklist.
 *
 * @author vssavin on 29.08.2023
 */
@Service
public class SimpleAuthService implements AuthService {

	private static final Logger log = LoggerFactory.getLogger(SimpleAuthService.class);

	private static final Class<? extends Authentication> authenticationClass = CustomUsernamePasswordAuthenticationToken.class;

	private static final ConcurrentHashMap<String, Integer> blackList = new ConcurrentHashMap<>(50);

	private static final ConcurrentHashMap<String, Long> banExpirationTime = new ConcurrentHashMap<>(50);

	private final UserService userService;

	private final EventService eventService;

	private final SecureService secureService;

	private final PasswordEncoder passwordEncoder;

	private final int maxFailureCount;

	private final int blockTimeMinutes;

	@Autowired
	public SimpleAuthService(UserService userService, EventService eventService, UmConfig umConfig,
			PasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.eventService = eventService;
		this.secureService = umConfig.getSecureService();
		this.passwordEncoder = passwordEncoder;
		this.maxFailureCount = umConfig.getMaxAuthFailureCount();
		this.blockTimeMinutes = umConfig.getAuthFailureBlockTime();
	}

	@Override
	public Authentication authenticate(Authentication authentication) {
		Object credentials = authentication.getCredentials();
		Object userName = authentication.getPrincipal();
		if (credentials != null) {
			UserDetails user = userService.loadUserByUsername(userName.toString());
			if (user != null) {
				checkUserDetails(user);

				String addr = getRemoteAddress(authentication);
				String password = secureService.decrypt(credentials.toString(), secureService.getPrivateKey(addr));

				if (passwordEncoder.matches(password, user.getPassword())) {
					List<GrantedAuthority> authorities = new ArrayList<>(user.getAuthorities());
					return new CustomUsernamePasswordAuthenticationToken(authentication.getPrincipal(), password,
							authorities);
				}
				else {
					throw new BadCredentialsException("Authentication failed");
				}

			}
			else {
				return authentication;
			}

		}
		else {
			return authentication;
		}
	}

	@Override
	@Transactional
	@UmRouteDatasource
	public Collection<GrantedAuthority> processSuccessAuthentication(Authentication authentication,
			HttpServletRequest request, EventType eventType) {
		User user = null;
		try {
			OAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
			user = userService.processOAuthPostLogin(oAuth2User);
		}
		catch (ClassCastException e) {
			// ignore, it's ok
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
		return user.getAuthorities()
			.stream()
			.map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
			.collect(Collectors.toList());
	}

	@Override
	public boolean isAuthenticationAllowed(String ipAddress) {
		int failureCounts = getFailureCount(ipAddress);
		if (failureCounts >= maxFailureCount) {
			long expireTime = getBanExpirationTime(ipAddress);
			if (expireTime > 0 && System.currentTimeMillis() < expireTime) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void processFailureAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) {
		String userIp = request.getRemoteAddr();
		int failureCount = getFailureCount(userIp);

		if (failureCount >= maxFailureCount) {
			long expirationTime = getBanExpirationTime(userIp);
			if (expirationTime == 0) {
				blockIp(userIp);
				log.info("IP {} has been blocked!", userIp);
				throw new AuthenticationForbiddenException("Sorry! You have been blocked! Try again later!");
			}
			else if (expirationTime < System.currentTimeMillis()) {
				resetFailureCount(userIp);
				incrementFailureCount(userIp);
			}
			else {
				throw new AuthenticationForbiddenException("Sorry! You have been blocked! Try again later!");
			}
		}
		else {
			incrementFailureCount(userIp);
		}
	}

	@Override
	public Class<? extends Authentication> authenticationClass() {
		return authenticationClass;
	}

	private void saveUserEvent(User user, HttpServletRequest request, EventType eventType) {
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

	private int getFailureCount(String userIp) {
		return blackList.getOrDefault(userIp, 1);
	}

	private void incrementFailureCount(String userIp) {
		int failureCounts = getFailureCount(userIp) + 1;
		blackList.put(userIp, failureCounts);
	}

	private void resetFailureCount(String userIp) {
		blackList.put(userIp, 1);
		banExpirationTime.remove(userIp);
	}

	private long getBanExpirationTime(String userIp) {
		return banExpirationTime.getOrDefault(userIp, 0L);
	}

	private void blockIp(String ip) {
		banExpirationTime.put(ip, Calendar.getInstance().getTimeInMillis() + ((long) blockTimeMinutes * 60 * 1000));
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

	private String getRemoteAddress(Authentication authentication) {
		Object details = authentication.getDetails();

		if (details instanceof WebAuthenticationDetails) {
			return ((WebAuthenticationDetails) details).getRemoteAddress();
		}

		return "";
	}

}

package com.github.vssavin.umlib.security.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author vssavin on 18.12.2021
 */
@Component
class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationFailureHandler.class);

    private static final String FAILURE_REDIRECT_PAGE = "/login.html?error=true";
    private static final int MAX_FAILURE_COUNTS = 3;
    private static final int BANNED_TIME_MINUTES = 60;
    private static final ConcurrentHashMap<String, Integer> blackList = new ConcurrentHashMap<>(50);
    private static final ConcurrentHashMap<String, Long> banExpireTimes = new ConcurrentHashMap<>(50);

    static boolean isBannedIp(String ipAddress) {
        Integer failureCounts = blackList.get(ipAddress);
        if (failureCounts == null) {
            return false;
        } else {
            if (failureCounts >= MAX_FAILURE_COUNTS) {
                Long expireTime = banExpireTimes.get(ipAddress);
                if (expireTime != null) {
                    if (Calendar.getInstance().getTimeInMillis() > expireTime) {
                        expireTime = 0L;
                        banExpireTimes.put(ipAddress, expireTime);
                        failureCounts = 0;
                        blackList.put(ipAddress, failureCounts);
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException {

        String userIp = request.getRemoteAddr();
        Integer failureCounts = blackList.get(userIp);
        String lang = request.getParameter("lang");
        if (lang != null) lang = "&lang=" + lang;
        else lang = "";
        if (failureCounts == null) {
            blackList.put(userIp, 1);
            response.sendRedirect(FAILURE_REDIRECT_PAGE + lang);
        } else {
            if (failureCounts < MAX_FAILURE_COUNTS) {
                failureCounts++;
                blackList.put(userIp, failureCounts);
                if (failureCounts >= MAX_FAILURE_COUNTS) {
                    banExpireTimes.put(userIp,
                            Calendar.getInstance().getTimeInMillis() + (BANNED_TIME_MINUTES * 60 * 1000));
                    log.info("IP " + userIp + " has been banned!");
                    response.sendError(HttpStatus.FORBIDDEN.value(), "Доступ запрещен");
                } else {
                    response.sendRedirect(FAILURE_REDIRECT_PAGE + lang);
                }
            }
        }
    }
}

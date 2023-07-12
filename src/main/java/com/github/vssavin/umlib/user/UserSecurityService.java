package com.github.vssavin.umlib.user;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * @author vssavin on 12.07.2023
 */
@Service
public interface UserSecurityService {
    String getAuthorizedUserName(HttpServletRequest request);
    String getAuthorizedUserLogin(HttpServletRequest request);
    boolean isAuthorizedAdmin(HttpServletRequest request);
    boolean isAuthorizedUser(HttpServletRequest request);
}

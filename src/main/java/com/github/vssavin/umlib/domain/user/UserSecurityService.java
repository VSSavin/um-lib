package com.github.vssavin.umlib.domain.user;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * Main interface for checking user authorization.
 *
 * @author vssavin on 12.07.2023
 */
@Service
public interface UserSecurityService {

    String getAuthorizedUserName(HttpServletRequest request);

    String getAuthorizedUserLogin(HttpServletRequest request);

    boolean isAuthorizedAdmin(HttpServletRequest request);

    boolean isAuthorizedUser(HttpServletRequest request);

}

package com.github.vssavin.umlib.config;

import com.github.vssavin.umlib.user.Role;

/**
 * @author vssavin on 18.07.2023
 */
public enum Permission {
    USER_ADMIN(new String[]{Role.ROLE_USER.toString(), Role.ROLE_ADMIN.toString()}),
    ADMIN_ONLY(new String[]{Role.ROLE_ADMIN.toString()}),
    ANY_USER(new String[]{}),
    ;

    private final String[] roles;

    Permission(String[] roles) {
        this.roles = roles;
    }

    public String[] getRoles() {
        return roles;
    }
}

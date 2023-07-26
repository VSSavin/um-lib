package com.github.vssavin.umlib.config;

import com.github.vssavin.umlib.user.Role;

import static com.github.vssavin.umlib.user.Role.ROLE_ADMIN;
import static com.github.vssavin.umlib.user.Role.ROLE_USER;

/**
 * Enum with available permissions.
 *
 * @author vssavin on 18.07.2023
 */
public enum Permission {
    USER_ADMIN(new String[]{Role.getStringRole(ROLE_USER), Role.getStringRole(ROLE_ADMIN)}),
    ADMIN_ONLY(new String[]{Role.getStringRole(ROLE_ADMIN)}),
    ANY_USER(new String[]{});

    private final String[] roles;

    Permission(String[] roles) {
        this.roles = roles;
    }

    public String[] getRoles() {
        return roles;
    }
}

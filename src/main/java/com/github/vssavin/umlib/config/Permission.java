package com.github.vssavin.umlib.config;

import com.github.vssavin.umlib.domain.user.Role;

import static com.github.vssavin.umlib.domain.user.Role.ROLE_ADMIN;
import static com.github.vssavin.umlib.domain.user.Role.ROLE_USER;

/**
 * Enum with available permissions.
 *
 * @author vssavin on 18.07.2023
 */
public enum Permission {

    USER_ADMIN(Role.getStringRole(ROLE_USER), Role.getStringRole(ROLE_ADMIN)),
    ADMIN_ONLY(Role.getStringRole(ROLE_ADMIN)), ANY_USER();

    private final String[] roles;

    Permission(String... roles) {
        this.roles = roles;
    }

    public String[] getRoles() {
        return roles;
    }

}

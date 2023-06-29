package com.github.vssavin.umlib.user;

/**
 * @author vssavin on 23.12.21
 */
public enum Role {
    ROLE_USER,
    ROLE_ADMIN;

    public static Role getRole(String role) {
        if (role == null) return Role.ROLE_USER;
        return role.toLowerCase().contains("admin") ? Role.ROLE_ADMIN : Role.ROLE_USER;
    }
}


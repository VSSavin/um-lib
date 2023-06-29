package com.github.vssavin.umlib.config;

import java.util.Arrays;

/**
 * Created by vssavin on 17.05.2022.
 */
public class AuthorizedUrlPermission {
    private final String url;
    private final String[] roles;

    public AuthorizedUrlPermission(String url, String[] roles) {
        this.url = url;
        this.roles = roles;
    }

    public String getUrl() {
        return url;
    }

    public String[] getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        return "AuthorizedUrlPermission{" +
                "url='" + url + '\'' +
                ", roles=" + Arrays.toString(roles) +
                '}';
    }
}

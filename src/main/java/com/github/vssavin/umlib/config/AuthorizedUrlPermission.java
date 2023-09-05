package com.github.vssavin.umlib.config;

import org.springframework.http.HttpMethod;

import java.util.Arrays;

/**
 * Immutable class for storing url permissions.
 *
 * @author vssavin on 17.05.2022.
 */
public class AuthorizedUrlPermission {
    private static final String DEFAULT_HTTP_METHOD = HttpMethod.GET.name();
    private final String url;
    private final String httpMethod;
    private final String[] roles;

    public AuthorizedUrlPermission(String url, String httpMethod, Permission permission) {
        this.url = url;
        this.httpMethod = httpMethod;
        this.roles = permission.getRoles();
    }

    public AuthorizedUrlPermission(String url, Permission permission) {
        this.url = url;
        this.httpMethod = DEFAULT_HTTP_METHOD;
        this.roles = permission.getRoles();
    }

    public String getUrl() {
        return url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String[] getRoles() {
        return roles;
    }

    public static String getDefaultHttpMethod() {
        return DEFAULT_HTTP_METHOD;
    }

    @Override
    public String toString() {
        return "AuthorizedUrlPermission{" +
                "url='" + url + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", roles=" + Arrays.toString(roles) +
                '}';
    }
}

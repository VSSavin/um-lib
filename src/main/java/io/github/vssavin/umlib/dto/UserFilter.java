package io.github.vssavin.umlib.dto;

/**
 * Created by vssavin on 05.08.2022.
 */
public class UserFilter {
    private Long userId;
    private String login;
    private String name;
    private String email;

    public UserFilter(Long userId, String login, String name, String email) {
        this.userId = userId;
        this.login = login;
        this.name = name;
        this.email = email;
    }

    public static UserFilter emptyUserFilter() {
        return new UserFilter(null, null, null, null);
    }

    public boolean isEmpty() {
        return userId == null && login == null && name == null && email == null;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

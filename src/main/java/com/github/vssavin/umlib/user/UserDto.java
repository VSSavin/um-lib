package com.github.vssavin.umlib.user;

import java.util.Objects;

/**
 * @author vssavin on 05.08.2022.
 */
public class UserDto {
    private Long id;
    private String login;
    private String name;
    private String email;
    private boolean accountLocked;
    private boolean credentialsExpired;
    private boolean enabled = true;

    public UserDto(Long id, String login, String name, String email,
                   boolean accountLocked, boolean credentialsExpired, boolean enabled) {
        this.id = id;
        this.login = login;
        this.name = name;
        this.email = email;
        this.accountLocked = accountLocked;
        this.credentialsExpired = credentialsExpired;
        this.enabled = enabled;
    }

    public UserDto() {
    }

    public static UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getLogin(), user.getName(), user.getEmail(),
                !user.isAccountNonLocked(), !user.isCredentialsNonExpired(), user.isEnabled());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public boolean isCredentialsExpired() {
        return credentialsExpired;
    }

    public void setCredentialsExpired(boolean credentialsExpired) {
        this.credentialsExpired = credentialsExpired;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserDto userDto = (UserDto) o;
        return accountLocked == userDto.accountLocked && credentialsExpired == userDto.credentialsExpired &&
                enabled == userDto.enabled && id.equals(userDto.id) && login.equals(userDto.login) &&
                name.equals(userDto.name) && email.equals(userDto.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, login, name, email, accountLocked, credentialsExpired, enabled);
    }
}

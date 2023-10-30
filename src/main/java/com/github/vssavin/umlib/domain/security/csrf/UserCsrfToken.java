package com.github.vssavin.umlib.domain.security.csrf;

import javax.persistence.*;
import java.util.Date;

/**
 * Base csrf entity.
 *
 * @author vssavin on 30.10.2023
 */
@Entity
@Table(name = "csrf_tokens")
public class UserCsrfToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "token")
    private String token;

    @Column(name = "expiration_date")
    private Date expirationDate;

    public UserCsrfToken(Long userId, String token, Date expirationDate) {
        this.userId = userId;
        this.token = token;
        this.expirationDate = expirationDate;
    }

    public UserCsrfToken() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserCsrfToken that = (UserCsrfToken) o;

        if (!userId.equals(that.userId)) {
            return false;
        }
        if (!token.equals(that.token)) {
            return false;
        }
        return expirationDate.equals(that.expirationDate);
    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + token.hashCode();
        result = 31 * result + expirationDate.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "UserCsrfToken{" + "id=" + id + ", userId=" + userId + ", token='" + token + '\'' + ", expirationDate="
                + expirationDate + '}';
    }

}

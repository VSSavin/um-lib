package io.github.vssavin.umlib.entity;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * @author vssavin on 18.12.2021
 */
@Entity
@Table(name = "users")
public class User {
    public static final int EXPIRATION_DAYS = 1;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String login;
    private String name;
    private String password;
    private String email;
    private String authority;
    @Column(name = "expiration_date")
    private Date expirationDate;
    @Column(name = "verification_id")
    private String verificationId;

    public User(String login, String name, String password, String email, String authority) {
        this.login = login;
        this.name = name;
        this.password = password;
        this.email = email;
        this.authority = authority;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, EXPIRATION_DAYS);
        expirationDate = calendar.getTime();
        verificationId = UUID.randomUUID().toString();
    }

    public User() {
    }

    public String getLogin() {
        return login;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public Long getId() {
        return id;
    }

    public String getAuthority() {
        return authority;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public String getVerificationId() {
        return verificationId;
    }

    public String getEmail() {
        return email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static final class UserBuilder {
        private Long id;
        private String login;
        private String name;
        private String password;
        private String email;
        private String authority;
        private Date expirationDate;
        private String verificationId;

        private UserBuilder(){}

        public UserBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder login(String login) {
            this.login = login;
            return this;
        }

        public UserBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder authority(String authority) {
            this.authority = authority;
            return this;
        }

        public UserBuilder expirationDate(Date expirationDate) {
            this.expirationDate = expirationDate;
            return this;
        }

        public UserBuilder verificationId(String verificationId) {
            this.verificationId = verificationId;
            return this;
        }

        public User build() {
            User user = new User();
            user.id = id;
            user.login = login;
            user.name = name;
            user.password = password;
            user.email = email;
            user.authority = authority;
            user.expirationDate = expirationDate;
            user.verificationId = verificationId;
            return user;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return login.equals(user.login) && email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, email);
    }

    @Override
    public String toString() {
        return "User{" +
                "login='" + login + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", authority='" + authority + '\'' +
                ", expirationDate=" + expirationDate +
                ", verificationId='" + verificationId + '\'' +
                '}';
    }
}

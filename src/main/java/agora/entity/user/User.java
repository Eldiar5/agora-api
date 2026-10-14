package agora.entity.user;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public abstract class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    protected Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "usertype")
    protected UserType userType;

    @Column(name = "login")
    protected String login;

    @Column(name = "password_hash")
    protected String passwordHash;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "registered", insertable = false, updatable = false)
    protected LocalDateTime registrationDate;

    public User() {}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.login;
    }

    public abstract String beautify();

    public abstract String userName();

    public abstract String toFile();

}

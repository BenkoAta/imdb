package hu.benkoata.imdb.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@Table(name = "users")
@Entity
@Getter
@Setter
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private long id;
    @Column(nullable = false)
    private String fullName;
    @Column(unique = true, length = 100, nullable = false)
    @Getter()
    private String email;

    @Override
    public String getUsername() {
        return email;
    }

    @Column(nullable = false)
    private String password;
    @CreationTimestamp
    @Column(updatable = false, columnDefinition = "datetime")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(insertable = false, columnDefinition = "datetime")
    private LocalDateTime updatedAt;
    @Column(length = 32)
    private String gAuthKey;
    private int emailVerificationCode;
    private boolean accountLocked = true;
    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}

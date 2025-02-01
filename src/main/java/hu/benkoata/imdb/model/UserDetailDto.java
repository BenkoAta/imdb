package hu.benkoata.imdb.model;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Data
public class UserDetailDto implements UserDetails {
    private long id;
    private String fullName;
    private String email;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String gAuthKey;
    private int emailVerificationCode;
    private boolean accountLocked;
    private boolean emailVerified;
    private int deleteCode;
    private Integer resetPasswordCode;
    private LocalDateTime resetPasswordUntil;
    @Override
    public String getUsername() {
        return email;
    }
    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

}

package hu.benkoata.imdb.entities;

import hu.benkoata.imdb.dtos.CreateUserCommand;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;
import java.util.function.UnaryOperator;

@Table(name = "users")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private long id;
    @Column(nullable = false)
    private String fullName;
    @Column(unique = true, length = 100, nullable = false)
    @Getter()
    private String email;
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
    private boolean emailVerified = false;
    private int deleteCode;
    private Integer resetPasswordCode;
    @Column(insertable = false, columnDefinition = "datetime")
    private LocalDateTime resetPasswordUntil;

    public User(CreateUserCommand command, UnaryOperator<String> pwdEncoder, String gAuthKey, Random random) {
        this();
        email = command.getEmail();
        emailVerificationCode = random.nextInt(100_000, 1_000_000);
        fullName = command.getFullName();
        this.password = pwdEncoder.apply(command.getPassword());
        this.gAuthKey = gAuthKey;
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

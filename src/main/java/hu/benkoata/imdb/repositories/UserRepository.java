package hu.benkoata.imdb.repositories;

import hu.benkoata.imdb.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
@SuppressWarnings("unused")
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

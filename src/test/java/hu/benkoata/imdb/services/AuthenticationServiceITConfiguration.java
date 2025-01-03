package hu.benkoata.imdb.services;

import hu.benkoata.imdb.repositories.UserRepository;
import hu.benkoata.imdb.services.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class AuthenticationServiceITConfiguration {
    @Value("${security.jwt.secret-key}")
    private String secretKey;
    @Value("${security.jwt.expiration-secs}")
    private long jwtExpirationSecs;
    @SuppressWarnings("unused")
    @Bean
    AuthenticationService getAuthenticationService(UserRepository userRepository,
                                                   PasswordEncoder passwordEncoder) {
        return new AuthenticationService(userRepository,
                passwordEncoder,
                null,
                new JwtService(secretKey, jwtExpirationSecs));
    }
}

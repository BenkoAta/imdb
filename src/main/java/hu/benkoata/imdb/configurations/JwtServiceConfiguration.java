package hu.benkoata.imdb.configurations;

import hu.benkoata.imdb.services.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtServiceConfiguration {
    @Value("${security.jwt.secret-key}")
    private String secretKey;
    @Value("${security.jwt.expiration-secs}")
    private long jwtExpirationSecs;
    @Bean
    JwtService jwtService() {
        return new JwtService(secretKey, jwtExpirationSecs);
    }
}

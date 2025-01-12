package hu.benkoata.imdb.services;

import hu.benkoata.imdb.repositories.UserRepository;
import hu.benkoata.imdb.services.security.GoogleAuthenticatorService;
import hu.benkoata.imdb.services.security.JwtService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

@TestConfiguration
public class AuthenticationServiceITConfiguration {
    @Value("${security.jwt.secret-key}")
    private String secretKey;
    @Value("${security.jwt.expiration-secs}")
    private long jwtExpirationSecs;
    @SuppressWarnings("unused")
    @Bean
    public AuthenticationService authenticationService(ModelMapper modelMapper,
                                                          UserRepository userRepository,
                                                          PasswordEncoder passwordEncoder) {
        return new AuthenticationService(modelMapper,
                userRepository,
                passwordEncoder,
                null,
                new JwtService(secretKey, jwtExpirationSecs),
                new GoogleAuthenticatorService());
    }
    @SuppressWarnings("unused")
    @Bean
    HandlerExceptionResolver handlerExceptionResolver() {
        return (request, response, handler, ex) -> new ModelAndView();
    }
}

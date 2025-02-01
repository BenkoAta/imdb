package hu.benkoata.imdb.services;

import hu.benkoata.imdb.configurations.JwtServiceConfiguration;
import hu.benkoata.imdb.configurations.ModelMapperConfiguraion;
import hu.benkoata.imdb.repositories.UserRepository;
import hu.benkoata.imdb.services.security.GoogleAuthenticatorService;
import hu.benkoata.imdb.services.security.JwtService;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

@TestConfiguration
@Import({JwtServiceConfiguration.class,
        ModelMapperConfiguraion.class,
        GoogleAuthenticatorService.class})
public class AuthenticationServiceITConfiguration {
    @SuppressWarnings("unused")
    @Bean
    public AuthenticationService authenticationService(ModelMapper modelMapper,
                                                       UserRepository userRepository,
                                                       PasswordEncoder passwordEncoder,
                                                       AuthenticationManager authenticationManager,
                                                       JwtService jwtService,
                                                       GoogleAuthenticatorService googleAuthenticatorService) {
        return new AuthenticationService(modelMapper,
                userRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                googleAuthenticatorService);
    }

    @SuppressWarnings("unused")
    @Bean
    HandlerExceptionResolver handlerExceptionResolver() {
        return (request, response, handler, ex) -> new ModelAndView();
    }
}

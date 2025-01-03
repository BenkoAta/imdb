package hu.benkoata.imdb.services;

import hu.benkoata.imdb.dtos.CreateUserCommand;
import hu.benkoata.imdb.dtos.CreateUserDto;
import hu.benkoata.imdb.dtos.CredentialsCommand;
import hu.benkoata.imdb.dtos.JwtTokenDto;
import hu.benkoata.imdb.entities.User;
import hu.benkoata.imdb.exceptions.AccountNotFoundException;
import hu.benkoata.imdb.repositories.UserRepository;
import hu.benkoata.imdb.services.security.JwtService;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class AuthenticationService {
    private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public CreateUserDto createUser(String requestURI, CreateUserCommand command) {
        validate(command);
        User user = new User();
        user.setEmail(command.getEmail());
        user.setFullName(command.getFullName());
        user.setPassword(passwordEncoder.encode(command.getPassword()));
        User savedUser = userRepository.save(user);
        return new CreateUserDto(savedUser.getId(),"");
    }

    @SuppressWarnings({"java:S1135", "unused"})
    private void validate(CreateUserCommand command) {
        //todo validálás
    }

    public JwtTokenDto authenticate(String requestURI, CredentialsCommand credentials) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        credentials.getUsername(),
                        credentials.getPassword()
                )
        );
        User authenticatedUser = userRepository.findByEmail(credentials.getUsername())
                .orElseThrow(() -> new AccountNotFoundException(requestURI, credentials.getUsername()));
        String token = jwtService.generateToken(authenticatedUser);
        return new JwtTokenDto(token, jwtService.getExpirationTime(token, null));
    }
}

package hu.benkoata.imdb.services;

import hu.benkoata.imdb.dtos.*;
import hu.benkoata.imdb.entities.User;
import hu.benkoata.imdb.exceptions.UserNotFoundException;
import hu.benkoata.imdb.exceptions.WrongVerificationCodeException;
import hu.benkoata.imdb.repositories.UserRepository;
import hu.benkoata.imdb.services.security.GoogleAuthenticatorService;
import hu.benkoata.imdb.services.security.JwtService;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class AuthenticationService {
    private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final GoogleAuthenticatorService googleAuthenticatorService;

    @Transactional
    public CreateUserDto createUser(String requestURI, CreateUserCommand command) {
        validate(command);
        User user = new User();
        setFields(user, command, new Random());
        User savedUser = userRepository.save(user);
        return new CreateUserDto(savedUser.getId(),
                googleAuthenticatorService.getQRUrl(savedUser.getUsername(), savedUser.getGAuthKey()));
    }

    private void setFields(User user, CreateUserCommand command, Random random) {
        user.setEmail(command.getEmail());
        user.setEmailVerificationCode(random.nextInt(100_000, 1_000_000));
        user.setFullName(command.getFullName());
        user.setPassword(passwordEncoder.encode(command.getPassword()));
        user.setGAuthKey(googleAuthenticatorService.getKey());
    }

    @SuppressWarnings({"java:S1135", "unused"})
    private void validate(CreateUserCommand command) {
        //todo validálás
    }

    public JwtTokenDto authenticate(String requestURI, CredentialsCommand credentials) {
        User authenticatedUser = authenticate(requestURI, credentials.getUsername(), credentials.getPassword(),
                credentials.getTotpCode());
        String token = jwtService.generateToken(authenticatedUser);
        return new JwtTokenDto(token, jwtService.getExpirationTime(token, null));
    }

    private User authenticate(String requestURI, String username, String password, int totpCode) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        User result = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException(requestURI, username));
        googleAuthenticatorService.authenticate(requestURI, result, totpCode);
        return result;
    }

    public void enableUserIfVerificationOk(String requestURI, long id, int verificationCode) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(requestURI, id));
        if (verificationCode != user.getEmailVerificationCode()) {
            throw new WrongVerificationCodeException(requestURI, verificationCode);
        }
        user.setEmailVerificationCode(0);
        user.setAccountLocked(false);
        userRepository.save(user);
    }

    public UserDto getUserDetails(UserDetails userDetails) {
        return modelMapper.map(userDetails, UserDto.class);
    }

    public UserDto getUserDetails(String requestURI, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(requestURI, email));
        UserDto result = new UserDto();
        result.setEmail(user.getEmail());
        result.setId(user.getId());
        return result;
    }

    public void changePassword(String requestURI, ChangeCredentialsCommand chCredentials) {
        User authenticatedUser = authenticate(requestURI, chCredentials.getUsername(), chCredentials.getPassword(),
                chCredentials.getTotpCode());
        authenticatedUser.setPassword(passwordEncoder.encode(chCredentials.getNewPassword()));
        userRepository.save(authenticatedUser);
    }
}

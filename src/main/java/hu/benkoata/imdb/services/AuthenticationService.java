package hu.benkoata.imdb.services;

import hu.benkoata.imdb.dtos.*;
import hu.benkoata.imdb.entities.User;
import hu.benkoata.imdb.exceptions.EmailNotVerifiedException;
import hu.benkoata.imdb.exceptions.UserNotFoundException;
import hu.benkoata.imdb.exceptions.WrongVerificationCodeException;
import hu.benkoata.imdb.repositories.UserRepository;
import hu.benkoata.imdb.services.security.GoogleAuthenticatorService;
import hu.benkoata.imdb.services.security.JwtService;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;
import java.util.function.ObjIntConsumer;

@Slf4j
@Service
@RequiredArgsConstructor
@Getter
@SuppressWarnings("unused")
public class AuthenticationService {
    private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;
    private final Random random = new Random();
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final GoogleAuthenticatorService googleAuthenticatorService;

    @Transactional
    public CreateUserDto createUser(String requestURI, CreateUserCommand command, ObjIntConsumer<UserDto> mailFunction) {
        validate(command);
        User user = new User(command, passwordEncoder::encode, googleAuthenticatorService.getKey(), random);
        User savedUser = userRepository.save(user);
        mailFunction.accept(modelMapper.map(savedUser, UserDto.class), savedUser.getEmailVerificationCode());
        return new CreateUserDto(savedUser.getId(),
                googleAuthenticatorService.getQRUrl(savedUser.getEmail(), savedUser.getGAuthKey()));
    }

    public void unlockUser(String requestURI, long userId, ObjIntConsumer<UserDto> mailFunction) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(requestURI, userId));
        user.setEmailVerificationCode(getVerificationCode());
        userRepository.save(user);
        mailFunction.accept(modelMapper.map(user, UserDto.class), user.getEmailVerificationCode());
    }

    private int getVerificationCode() {
        return random.nextInt(100_000, 1_000_000);
    }

    @SuppressWarnings({"java:S1135", "unused"})
    private void validate(CreateUserCommand command) {
        //todo validálás
    }

    public JwtTokenDto authenticate(String requestURI, CredentialsCommand credentials) {
        User authenticatedUser = authenticate(requestURI, credentials.getUsername(), credentials.getPassword(),
                credentials.getTotpCode());
        String token = jwtService.generateToken(authenticatedUser.getEmail(), new Date(System.currentTimeMillis()));
        return new JwtTokenDto(token, jwtService.extractExpirationTime(token, null));
    }
    private User authenticate(String requestURI, String username, String password, int totpCode) {
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        return authenticateWithTotpOnly(requestURI, username, totpCode);
    }
    private User authenticateWithTotpOnly(String requestURI, String username, int totpCode) {
        User result = userRepository.findByEmail(username)
                .orElseThrow(() -> new BadCredentialsException("User not found with name: " + username));
        clearResetPasswordCode(result);
        googleAuthenticatorService.authenticate(requestURI, result.getGAuthKey(), totpCode);
        return result;
    }
    private void clearResetPasswordCode(User user) {
        if (user.getResetPasswordCode() != null) {
            user.setResetPasswordCode(null);
            user.setResetPasswordUntil(null);
            userRepository.save(user);
        }
    }
    public void enableUserIfVerificationOk(String requestURI, long id, int verificationCode) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(requestURI, id));
        if (verificationCode != user.getEmailVerificationCode()) {
            throw new WrongVerificationCodeException(requestURI, verificationCode);
        }
        user.setEmailVerificationCode(0);
        user.setEmailVerified(true);
        user.setAccountLocked(false);
        userRepository.save(user);
    }

    public UserDto getFullUserDetails(String requestURI, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(requestURI, email));
        return modelMapper.map(user, UserDto.class);
    }

    public UserDto getUserDetailsByEmail(String requestURI, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(requestURI, email));
        UserDto result = new UserDto();
        result.setEmail(user.getEmail());
        result.setId(user.getId());
        return result;
    }

    public void changePassword(String requestURI, ChangeCredentialsCommand chCredentials, LocalDateTime referenceDateTime) {
        Integer resetPasswordCode = getResetPasswordCodeOrNull(requestURI, chCredentials.getUsername(), referenceDateTime);
        User authenticatedUser;
        if (resetPasswordCode != null && resetPasswordCode.toString().equals(chCredentials.getPassword())) {
            authenticatedUser = authenticateWithTotpOnly(requestURI, chCredentials.getUsername(), chCredentials.getTotpCode());
        } else {
            authenticatedUser = authenticate(requestURI, chCredentials.getUsername(), chCredentials.getPassword(),
                    chCredentials.getTotpCode());
        }
        authenticatedUser.setPassword(passwordEncoder.encode(chCredentials.getNewPassword()));
        userRepository.save(authenticatedUser);
    }


    private Integer getResetPasswordCodeOrNull(String requestURI, String email, LocalDateTime referenceDateTime) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(requestURI, email));
        if (user.getResetPasswordUntil() != null && user.getResetPasswordUntil().isAfter(referenceDateTime)) {
            return user.getResetPasswordCode();
        }
        return null;
    }

    public boolean lockUserByIdOrSendEmail(String requestURI, long id, ObjIntConsumer<UserDto> mailFunction) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(requestURI, id));
        if (!user.isEmailVerified()) {
            user.setAccountLocked(true);
            userRepository.save(user);
            return true;
        } else {
            user.setDeleteCode(getVerificationCode());
            userRepository.save(user);
            mailFunction.accept(modelMapper.map(user, UserDto.class), user.getDeleteCode());
            return false;
        }
    }

    public void deleteUserById(String requestURI, long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(requestURI, id));
        if (!user.isEmailVerified() && user.isAccountLocked()) {
            userRepository.delete(user);
        }
    }

    public void lockUserById(String requestURI, long id, int verificationCode) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(requestURI, id));
        if (user.getDeleteCode() == verificationCode) {
            user.setAccountLocked(true);
            userRepository.save(user);
        } else {
            throw new WrongVerificationCodeException(requestURI, verificationCode);
        }
    }

    public void deleteUserById(String requestURI, long id, int verificationCode) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(requestURI, id));
        if (user.getDeleteCode() == verificationCode) {
            userRepository.delete(user);
        } else {
            throw new WrongVerificationCodeException(requestURI, verificationCode);
        }
    }

    public void resetPasswordForNextChange(String requestURI, long id,
                                           LocalDateTime referenceDateTime,
                                           ObjIntConsumer<UserDto> mailFunction) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(requestURI, id));
        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException(requestURI);
        }
        user.setResetPasswordCode(getVerificationCode());
        user.setResetPasswordUntil(referenceDateTime.plusMinutes(10));
        userRepository.save(user);
        mailFunction.accept(modelMapper.map(user, UserDto.class), user.getResetPasswordCode());
    }
}

package hu.benkoata.imdb.services;

import hu.benkoata.imdb.dtos.CreateUserCommand;
import hu.benkoata.imdb.repositories.UserRepository;
import hu.benkoata.imdb.services.security.GoogleAuthenticatorService;
import hu.benkoata.imdb.services.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    ModelMapper modelMapper = new ModelMapper();
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Mock
    UserRepository userRepository;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtService jwtService;
    @Mock
    GoogleAuthenticatorService googleAuthenticatorService = new GoogleAuthenticatorService();
    AuthenticationService authenticationService;
    static CreateUserCommand testCreateUserCommand = new CreateUserCommand("John Doe", "a@b.c", "d");
    @BeforeEach
    void init() {
        authenticationService = new AuthenticationService(modelMapper,
                userRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                googleAuthenticatorService);
    }
    @Test
    void testCreateUser() {
    }

    @Test
    void testUnlockUser() {
    }

    @Test
    void testAuthenticate() {
    }

    @Test
    void testEnableUserIfVerificationOk() {
    }

    @Test
    void testGetFullUserDetails() {
    }

    @Test
    void testGetUserDetailsByEmail() {
    }

    @Test
    void testChangePassword() {
    }

    @Test
    void testLockUserByIdOrSendEmail() {
    }

    @Test
    void testDeleteUserById() {
    }

    @Test
    void testLockUserByIdWithVerificationCode() {
    }

    @Test
    void testDeleteUserByIdWithVerificationCode() {
    }

    @Test
    void testResetPasswordForNextChange() {
    }
}
package hu.benkoata.imdb.services;

import hu.benkoata.imdb.dtos.*;
import hu.benkoata.imdb.entities.User;
import hu.benkoata.imdb.exceptions.TotpAuthenticationException;
import hu.benkoata.imdb.exceptions.WrongVerificationCodeException;
import hu.benkoata.imdb.repositories.UserRepository;
import hu.benkoata.imdb.services.security.GoogleAuthenticatorService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@DataJpaTest(showSql = false)
@EnableWebSecurity
@Import(AuthenticationServiceITConfiguration.class)
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SuppressWarnings("OptionalGetWithoutIsPresent")
class AuthenticationServiceIT {
    private static final String NEW_PASSWORD = "np";
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AuthenticationService authenticationService;
    @MockitoBean
    GoogleAuthenticatorService googleAuthenticatorService;
    private static final CreateUserCommand TEST_CREATE_USER_COMMAND = new CreateUserCommand("John Doe", "a@b.c", "d");

    @BeforeEach
    void init() {
        Optional<User> byEmail = userRepository.findByEmail("a@b.c");
        byEmail.ifPresent(userRepository::delete);
    }

    @Test
    @Order(1)
    void testCreateAccount() {
        AtomicReference<UserDto> sentUserDto = new AtomicReference<>();
        AtomicReference<Integer> sentVerificationCode = new AtomicReference<>();
        CreateUserDto actual = authenticationService.createUser(null, TEST_CREATE_USER_COMMAND, (userDto, integer) -> {
            sentUserDto.set(userDto);
            sentVerificationCode.set(integer);
        });
        assertThat(actual.getId()).isPositive();
        User user = userRepository.findById(actual.getId()).orElse(null);
        assertThat(user).isNotNull();
        assertThat(sentUserDto.get().getId()).isEqualTo(user.getId());
        assertThat(sentUserDto.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(sentUserDto.get().getFullName()).isEqualTo(user.getFullName());
        assertThat(sentVerificationCode.get()).isEqualTo(user.getEmailVerificationCode());
    }

    @Test
    @Order(2)
    void testEnableUserWrongVerification() {
        prepareUserRepositoryForEmailVerification(TEST_CREATE_USER_COMMAND);
        User user = userRepository.findByEmail(TEST_CREATE_USER_COMMAND.getEmail()).get();
        long id = user.getId();
        assertThrows(WrongVerificationCodeException.class,
                () -> authenticationService.enableUserIfVerificationOk(null, id, 0));
    }

    private void prepareUserRepositoryForEmailVerification(CreateUserCommand command) {
        Optional<User> byEmail = userRepository.findByEmail(command.getEmail());
        byEmail.ifPresentOrElse(user -> user.setPassword(command.getPassword()), () -> {
            String key = authenticationService.getGoogleAuthenticatorService().getKey();
            User user = new User(command,
                    authenticationService.getPasswordEncoder()::encode,
                    key,
                    authenticationService.getRandom());
            userRepository.save(user);
        });
    }

    @Test
    @Order(3)
    void testEnableUserGoodVerification() {
        prepareUserRepositoryForEmailVerification(TEST_CREATE_USER_COMMAND);
        User user = userRepository.findByEmail(TEST_CREATE_USER_COMMAND.getEmail()).get();
        authenticationService.enableUserIfVerificationOk(null, user.getId(), user.getEmailVerificationCode());
        user = userRepository.findByEmail(TEST_CREATE_USER_COMMAND.getEmail()).get();
        assertFalse(user.isAccountLocked());
    }

    @Test
    void testAuthenticateOk() {
        mockGoogleAuhenticationService(googleAuthenticatorService);
        prepareUserRepositoryForAuthenticationTest(TEST_CREATE_USER_COMMAND);
        CredentialsCommand credentials = new CredentialsCommand(TEST_CREATE_USER_COMMAND.getEmail(),
                TEST_CREATE_USER_COMMAND.getPassword(),
                123);
        JwtTokenDto token = authenticationService.authenticate("teszt", credentials);
        System.out.println(token);
    }

    @Test
    void testAuthenticateWrongPassword() {
        mockGoogleAuhenticationService(googleAuthenticatorService);
        prepareUserRepositoryForAuthenticationTest(TEST_CREATE_USER_COMMAND);
        CredentialsCommand credentials = new CredentialsCommand(TEST_CREATE_USER_COMMAND.getEmail(),
                "wrongpassword",
                123);
        assertThatThrownBy(() -> authenticationService.authenticate("teszt", credentials))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void testAuthenticateWithoutTotp() {
        mockGoogleAuhenticationService(googleAuthenticatorService);
        prepareUserRepositoryForAuthenticationTest(TEST_CREATE_USER_COMMAND);
        CredentialsCommand credentials = new CredentialsCommand(TEST_CREATE_USER_COMMAND.getEmail(), TEST_CREATE_USER_COMMAND.getPassword());
        assertThatThrownBy(() -> authenticationService.authenticate("teszt", credentials))
                .isInstanceOf(TotpAuthenticationException.class);
    }

    private void mockGoogleAuhenticationService(GoogleAuthenticatorService googleAuthenticatorService) {
        String mockedKey = "01234567890123456789";
        when(googleAuthenticatorService.getKey()).thenReturn(mockedKey);
        doAnswer(invocationOnMock -> {
            Integer totpCode = invocationOnMock.getArgument(2);
            if (totpCode != 123) {
                throw new TotpAuthenticationException("teszt");
            }
            return null;
        }).when(googleAuthenticatorService).authenticate(anyString(), eq(mockedKey), anyInt());
    }

    private void prepareUserRepositoryForAuthenticationTest(CreateUserCommand command) {
        Optional<User> byEmail = userRepository.findByEmail(command.getEmail());
        byEmail.ifPresentOrElse(user -> user.setPassword(command.getPassword()), () -> {
            String key = authenticationService.getGoogleAuthenticatorService().getKey();
            if (key != null) {
                assertThat(key).hasSize(20);
            }
            User user = new User(command,
                    authenticationService.getPasswordEncoder()::encode,
                    key,
                    authenticationService.getRandom());
            userRepository.save(user);
            user.setEmailVerificationCode(0);
            user.setEmailVerified(true);
            user.setAccountLocked(false);
            userRepository.save(user);
        });
    }

    @Test
    void testGetFullUserDetails() {
        prepareUserRepositoryForAuthenticationTest(TEST_CREATE_USER_COMMAND);
        UserDto actual = authenticationService.getFullUserDetails("teszt", TEST_CREATE_USER_COMMAND.getEmail());
        assertThat(actual.getId()).isPositive();
        assertEquals(TEST_CREATE_USER_COMMAND.getFullName(), actual.getFullName());
        assertEquals(TEST_CREATE_USER_COMMAND.getEmail(), actual.getEmail());
        assertThat(actual.getUpdatedAt()).isNotNull();
        LocalDateTime createdAt = actual.getCreatedAt();
        assertThat(Duration.between(createdAt, LocalDateTime.now()).getSeconds()).isLessThan(2);
    }

    @Test
    void testGetUserDetailsByEmail() {
        prepareUserRepositoryForAuthenticationTest(TEST_CREATE_USER_COMMAND);
        UserDto actual = authenticationService.getUserDetailsByEmail("teszt", TEST_CREATE_USER_COMMAND.getEmail());
        assertThat(actual.getId()).isPositive();
        assertThat(actual.getFullName()).isNull();
        assertEquals(TEST_CREATE_USER_COMMAND.getEmail(), actual.getEmail());
        assertThat(actual.getUpdatedAt()).isNull();
        assertThat(actual.getCreatedAt()).isNull();
    }
    @Test
    void testChangePasswordWithoutResetCodeOK() {
        mockGoogleAuhenticationService(googleAuthenticatorService);
        LocalDateTime referenceDate = LocalDateTime.now();
        prepareUserRepositoryForAuthenticationTest(TEST_CREATE_USER_COMMAND);
        ChangeCredentialsCommand command = new ChangeCredentialsCommand();
        command.setUsername(TEST_CREATE_USER_COMMAND.getEmail());
        command.setPassword(TEST_CREATE_USER_COMMAND.getPassword());
        command.setTotpCode(123);
        command.setNewPassword(NEW_PASSWORD);
        authenticationService.changePassword("test", command, referenceDate);
        User user = userRepository.findByEmail(TEST_CREATE_USER_COMMAND.getEmail()).orElse(null);
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, user.getPassword()));
    }
    @Test
    void testChangePasswordWithoutResetCodeWrongPassword() {
        mockGoogleAuhenticationService(googleAuthenticatorService);
        LocalDateTime referenceDate = LocalDateTime.now();
        prepareUserRepositoryForAuthenticationTest(TEST_CREATE_USER_COMMAND);
        ChangeCredentialsCommand command = new ChangeCredentialsCommand();
        command.setUsername(TEST_CREATE_USER_COMMAND.getEmail());
        command.setPassword(TEST_CREATE_USER_COMMAND.getPassword()+"1");
        command.setTotpCode(123);
        command.setNewPassword(NEW_PASSWORD);
        assertThatThrownBy(() -> authenticationService.changePassword("test", command, referenceDate))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void testChangePasswordWithoutResetCodeWrongTotp() {
        mockGoogleAuhenticationService(googleAuthenticatorService);
        LocalDateTime referenceDate = LocalDateTime.now();
        prepareUserRepositoryForAuthenticationTest(TEST_CREATE_USER_COMMAND);
        ChangeCredentialsCommand command = new ChangeCredentialsCommand();
        command.setUsername(TEST_CREATE_USER_COMMAND.getEmail());
        command.setPassword(TEST_CREATE_USER_COMMAND.getPassword());
        command.setTotpCode(1);
        command.setNewPassword(NEW_PASSWORD);
        assertThatThrownBy(() -> authenticationService.changePassword("test", command, referenceDate))
                .isInstanceOf(TotpAuthenticationException.class);
    }

    @Test
    @Disabled
    void testChangePasswordWithResetCodeNOK() {
        fail();
    }

    @Test
    @Disabled
    void testChangePasswordWithResetCodeOK() {
        fail();
    }

    @Test
    @Disabled
    void testLockUserByIdOrSendEmail() {
        //Todo kétféle teszt email verifikált: delete kód kitöltve és levélküldés
        // nem verifikált: lockolás
        // tesztelés mockolt userRepositoryval
        fail();
    }

    @Test
    @Disabled
    void testDeleteUserById() {
        //Todo csak akkor törölhető, ha email nincs verifikálva és lockolt
        // tesztelés mockolt userRepositoryval
        fail();
    }

    @Test
    @Disabled
    void testLockUserByIdWithVerification() {
        //Todo csak akkor lockolható, ha jó a verifikációs kód
        // tesztelés mockolt userRepositoryval
        fail();
    }

    @Test
    @Disabled
    void testDeleteUserByIdWithVerificationCode() {
        //Todo csak akkor lockolható, ha jó a verifikációs kód
        // tesztelés mockolt userRepositoryval
        fail();
    }

    @Test
    @Disabled
    void testResetPasswordForNextChange() {
        //Todo ha nem volt az email verifikálva, akkor exception
        // resetpasswordcode és resetpassworduntil ellenőrzése
        // levélküldés ellenőrzése
        fail();
    }
}
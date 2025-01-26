package hu.benkoata.imdb.services;

import hu.benkoata.imdb.ImdbApplication;
import hu.benkoata.imdb.configurations.ApplicationAuthenticationConfiguration;
import hu.benkoata.imdb.configurations.JwtAuthenticationFilter;
import hu.benkoata.imdb.configurations.SecurityConfig;
import hu.benkoata.imdb.dtos.CreateUserCommand;
import hu.benkoata.imdb.dtos.CreateUserDto;
import hu.benkoata.imdb.entities.User;
import hu.benkoata.imdb.exceptions.WrongVerificationCodeException;
import hu.benkoata.imdb.repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(showSql = false)
@ContextConfiguration(classes = {
        ImdbApplication.class,
        ApplicationAuthenticationConfiguration.class,
        JwtAuthenticationFilter.class,
        SecurityConfig.class,
        AuthenticationServiceITConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SuppressWarnings("OptionalGetWithoutIsPresent")
class AuthenticationServiceIT {
    @Value("${spring.datasource.url}")
    private String url;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AuthenticationService authenticationService;
    static CreateUserCommand testCreateUserCommand = new CreateUserCommand("John Doe", "a@b.c", "d");
    @BeforeAll
    void init() {
        assertThat(url).isEqualTo("jdbc:mariadb://localhost:3307/imdb");
        Optional<User> byEmail = userRepository.findByEmail("a@b.c");
        byEmail.ifPresent(userRepository::delete);
    }
    @Test
    @Order(1)
    void testCreateAccount() {
        CreateUserDto actual = authenticationService.createUser(null, testCreateUserCommand, (userDto, integer) -> {});
        assertThat(actual.getId()).isPositive();
        Optional<User> byId = userRepository.findById(actual.getId());
        assertThat(byId).isPresent();
        System.out.println(actual.getQrUrl());
    }
    @Test
    @Order(2)
    void testEnableUserWrongVerification() {
        User user = userRepository.findByEmail(testCreateUserCommand.getEmail()).get();
        long id = user.getId();
        assertThrows(WrongVerificationCodeException.class,
                () -> authenticationService.enableUserIfVerificationOk(null, id, 0));
    }
    @Test
    @Order(3)
    void testEnableUserGoodVerification() {
        User user = userRepository.findByEmail(testCreateUserCommand.getEmail()).get();
        authenticationService.enableUserIfVerificationOk(null, user.getId(), user.getEmailVerificationCode());
        user = userRepository.findByEmail(testCreateUserCommand.getEmail()).get();
        assertFalse(user.isAccountLocked());
    }

    @Test
    @Order(4)
    void testAuthenticate() {
        fail();
    }

    @Test
    @Order(5)
    void testGetFullUserDetails() {
        fail();
    }

    @Test
    @Order(6)
    void testGetUserDetailsByEmail() {
        fail();
    }
    @Test
    @Order(7)
    void testChangePasswordWithoutResetCodeNOK() {
        fail();
    }
    @Test
    @Order(8)
    void testChangePasswordWithoutResetCodeOK() {
        fail();
    }
    @Test
    @Order(9)
    void testChangePasswordWithResetCodeNOK() {
        fail();
    }
    @Test
    @Order(10)
    void testChangePasswordWithResetCodeOK() {
        fail();
    }

    @Test
    void testLockUserByIdOrSendEmail() {
        //Todo kétféle teszt email verifikált: delete kód kitöltve és levélküldés
        // nem verifikált: lockolás
        // tesztelés mockolt userRepositoryval
        fail();
    }

    @Test
    void testDeleteUserById() {
        //Todo csak akkor törölhető, ha email nincs verifikálva és lockolt
        // tesztelés mockolt userRepositoryval
        fail();
    }

    @Test
    void testLockUserByIdWithVerification() {
        //Todo csak akkor lockolható, ha jó a verifikációs kód
        // tesztelés mockolt userRepositoryval
        fail();
    }

    @Test
    void testDeleteUserByIdWithVerificationCode() {
        //Todo csak akkor lockolható, ha jó a verifikációs kód
        // tesztelés mockolt userRepositoryval
        fail();
    }
    @Test
    void testResetPasswordForNextChange() {
        //Todo ha nem volt az email verifikálva, akkor exception
        // resetpasswordcode és resetpassworduntil ellenőrzése
        // levélküldés ellenőrzése
        fail();
    }
}
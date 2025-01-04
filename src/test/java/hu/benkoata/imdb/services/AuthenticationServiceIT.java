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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        CreateUserDto actual = authenticationService.createUser(null, testCreateUserCommand);
        assertThat(actual.getId()).isPositive();
        Optional<User> byId = userRepository.findById(actual.getId());
        assertThat(byId).isPresent();
        System.out.println(actual.getQrUrl());
    }
    @Test
    @Order(2)
    void testEnableUserIfVerificationWrong() {
        User user = userRepository.findByEmail(testCreateUserCommand.getEmail()).get();
        long id = user.getId();
        assertThrows(WrongVerificationCodeException.class,
                () -> authenticationService.enableUserIfVerificationOk(null, id, 0));
    }
    @Test
    @Order(3)
    void testEnableUserIfVerificationOk() {
        User user = userRepository.findByEmail(testCreateUserCommand.getEmail()).get();
        authenticationService.enableUserIfVerificationOk(null, user.getId(), user.getEmailVerificationCode());
        user = userRepository.findByEmail(testCreateUserCommand.getEmail()).get();
        assertFalse(user.isAccountLocked());
    }
}
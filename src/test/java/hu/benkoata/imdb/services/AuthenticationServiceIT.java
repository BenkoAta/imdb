package hu.benkoata.imdb.services;

import hu.benkoata.imdb.ImdbApplication;
import hu.benkoata.imdb.configurations.ApplicationAuthenticationConfiguration;
import hu.benkoata.imdb.configurations.SecurityConfig;
import hu.benkoata.imdb.dtos.CreateUserCommand;
import hu.benkoata.imdb.dtos.CreateUserDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@ContextConfiguration(classes = {
        ImdbApplication.class,
        SecurityConfig.class,
        ApplicationAuthenticationConfiguration.class,
        AuthenticationServiceITConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class AuthenticationServiceIT {
    @Value("${spring.datasource.url}")
    private String url;
    @Autowired
    AuthenticationService authenticationService;

    @BeforeAll
    void init() {
        assertThat(url).isEqualTo("jdbc:mariadb://localhost:3307/imdb");
    }
    @Test
    void testCreateAccount() {
        CreateUserCommand command = new CreateUserCommand("John Doe", "a@b.c", "d");
        CreateUserDto actual = authenticationService.createUser(null, command);
        assertThat(actual.getId()).isPositive();
    }
}
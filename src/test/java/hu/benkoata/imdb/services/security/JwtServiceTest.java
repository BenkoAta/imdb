package hu.benkoata.imdb.services.security;

import hu.benkoata.imdb.entities.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    static final String SECRET_KEY = "3PdxtdcNdKsHc4l0uG7ExRT60eShCYAsXIK8dAEjQxDX3V/ENsljoDOMmWnupfYPVrk9pNDX3mOLvQJgaEpX9w==";
    static final long EXPIRATION_SECS = 3600;
    static final Date REFERENCE_DATE = Date.from(ZonedDateTime.of(
                    LocalDateTime.of(2025, 1, 3, 0, 0, 0),
                    ZoneId.of("UTC"))
            .toInstant());
    static final Date SHOULD_EXPIRED_DATE = Date.from(ZonedDateTime.of(
                    LocalDateTime.of(2025, 1, 3, 12, 0, 0),
                    ZoneId.of("UTC"))
            .toInstant());
    static final User USER = new User();

    static {
        USER.setEmail("a@b.c");
    }

    static final String EXPECTED_JWT = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhQGIuYyIsImlhdCI6MTczNTg2MjQwMCwiZXhwIjoxNzM1ODY2MDAwfQ.1bFqcfxHkSyg9RexSrXH7bPYi6wkO1L_n7eVv3LJQlHZESXWfVgjz2Uq7JttIQD7gozrjm6pXnzI-AxwaxTWDA";
    JwtService jwtService = new JwtService(SECRET_KEY, EXPIRATION_SECS);

    @Test
    void testCreateSecretKey() {
        System.out.println(Base64.getEncoder().encodeToString(Keys.secretKeyFor(JwtService.SIGNATURE_ALGORITHM).getEncoded()));
        System.out.println(Base64.getEncoder().encodeToString(Keys.secretKeyFor(JwtService.SIGNATURE_ALGORITHM).getEncoded()));
        System.out.println(Base64.getEncoder().encodeToString(Keys.secretKeyFor(JwtService.SIGNATURE_ALGORITHM).getEncoded()));
        assertTrue(true);
    }

    @Test
    void testGenerateToken() {
        assertEquals(EXPECTED_JWT, jwtService.generateToken(new HashMap<>(), USER, REFERENCE_DATE));
    }

    @Test
    void testExtractUsernameValid() {
        String actual = jwtService.extractUsername(EXPECTED_JWT, REFERENCE_DATE);
        assertThat(actual).isEqualTo(USER.getUsername());
    }
    @Test
    void testExtractUsernameExpired() {
        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(EXPECTED_JWT, SHOULD_EXPIRED_DATE));
    }
    @Test
    void testTokenValid() {
        assertTrue(jwtService.isTokenValid(EXPECTED_JWT, USER, REFERENCE_DATE));
    }
    @Test
    void testTokenExpired() {
        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenValid(EXPECTED_JWT, USER, SHOULD_EXPIRED_DATE));
    }
}
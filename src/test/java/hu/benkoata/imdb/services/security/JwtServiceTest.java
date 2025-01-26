package hu.benkoata.imdb.services.security;

import hu.benkoata.imdb.entities.User;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class JwtServiceTest {
    static final String SECRET_KEY = "3PdxtdcNdKsHc4l0uG7ExRT60eShCYAsXIK8dAEjQxDX3V/ENsljoDOMmWnupfYPVrk9pNDX3mOLvQJgaEpX9w==";
    static final long EXPIRATION_SECS = 3600;
    static final Date REFERENCE_DATE = Date.from(ZonedDateTime.of(
                    LocalDateTime.of(2025, 1, 3, 0, 0, 0),
                    ZoneId.systemDefault())
            .toInstant());
    static final Date SHOULD_EXPIRED_DATE = Date.from(ZonedDateTime.of(
                    LocalDateTime.of(2025, 1, 3, 12, 0, 0),
                    ZoneId.systemDefault())
            .toInstant());
    static final User USER = new User();

    static {
        USER.setEmail("a@b.c");
    }

    static final String EXPECTED_JWT = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhQGIuYyIsImlhdCI6MTczNTg1ODgwMCwiZXhwIjoxNzM1ODYyNDAwfQ.-wse5v74TH7tND7fBZ1Ee0H1x23YBAhVkkjw-fxOyXeb-bXetqV6kjV9RYiNg7I1oOgNXDPF6H0n1a180re79Q";
    JwtService jwtService = new JwtService(SECRET_KEY, EXPIRATION_SECS);

    @Test
    void testCreateSecretKey() {
        Set<String> secretKeys = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            secretKeys.add(JwtService.createSecretKey());
        }
        secretKeys.forEach(log::info);
        assertThat(secretKeys).hasSize(3);
    }

    @Test
    void testGenerateToken() {
        assertEquals(EXPECTED_JWT, jwtService.generateToken(USER.getUsername(), REFERENCE_DATE));
    }
    @Test
    void testGenerateTokenExtraClaims() {
        assertEquals(EXPECTED_JWT, jwtService.generateToken(new HashMap<>(), USER.getUsername(), REFERENCE_DATE));
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
    void testExtractExpirationTimeValid() {
        LocalDateTime actual = jwtService.extractExpirationTime(EXPECTED_JWT, REFERENCE_DATE);
        assertThat(actual).isEqualTo(REFERENCE_DATE.toInstant()
                .atZone(ZoneId.systemDefault())
                .plusSeconds(EXPIRATION_SECS)
                .toLocalDateTime());
    }
    @Test
    void testExtractExpirationTimeExpired() {
        assertThrows(ExpiredJwtException.class,() -> jwtService.extractExpirationTime(EXPECTED_JWT, SHOULD_EXPIRED_DATE));
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
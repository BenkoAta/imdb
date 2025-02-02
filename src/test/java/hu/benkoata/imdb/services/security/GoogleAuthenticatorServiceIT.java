package hu.benkoata.imdb.services.security;

import hu.benkoata.imdb.exceptions.TotpAuthenticationException;
import hu.benkoata.imdb.services.NtpService;
import org.awaitility.Durations;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

class GoogleAuthenticatorServiceIT {
    GoogleAuthenticatorService googleAuthenticatorService = new GoogleAuthenticatorService();
    ToptService toptService = new ToptService();
    private static final String SECRET_KEY = "TR2EFNOQ6SIU5YE7JCWWJHIAZNUYROZX";
    @BeforeAll
    static void init() {
        NtpService ntpService = new NtpService(1);
        ntpService.checkSync(LocalDateTime.now());
    }
    @Test
    void testGetKey() {
        String actual = googleAuthenticatorService.getKey();
        assertThat(actual).hasSize(32);
    }

    @Test
    void testGetQRUrl() {
        String actual = googleAuthenticatorService.getQRUrl("John Doe", "TR2EFNOQ6SIU5YE7JCWWJHIAZNUYROZX");
        assertThat(actual)
                .startsWith("otpauth://totp/John Doe?")
                .endsWith("?secret=TR2EFNOQ6SIU5YE7JCWWJHIAZNUYROZX&issuer=imdb");
    }

    @Test
    void testAuthenticate() {
        int successCounter = 0;
        int totpCode = toptService.generateToptCode(SECRET_KEY);
        for (int i = 0; i < 2; i++) {
            try {
                googleAuthenticatorService.authenticate("", SECRET_KEY, totpCode);
                successCounter++;
            } catch (TotpAuthenticationException tae) {
                await().atLeast(Durations.ONE_SECOND);
            }
        }
        assertThat(successCounter).isPositive();
    }
    @Test
    void testAuthenticateException() {
        toptService.setReferenceDateTime(LocalDateTime.now().minusSeconds(90));
        int totpCode = toptService.generateToptCode(SECRET_KEY);
        assertThatThrownBy(() -> googleAuthenticatorService.authenticate("", SECRET_KEY, totpCode))
                .isInstanceOf(TotpAuthenticationException.class);
    }
}
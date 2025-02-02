package hu.benkoata.imdb.services;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NtpServiceIT {
    NtpService ntpService = new NtpService(1);
    @Test
    void testSync() {
        assertThat(Math.abs(ntpService.checkSync(LocalDateTime.now()))).isLessThan(2);
    }
    @Test
    void testOutOfSync() {
        LocalDateTime future = LocalDateTime.now().plusSeconds(10);
        assertThatThrownBy(() -> ntpService.checkSync(future)).isInstanceOf(IllegalStateException.class);
    }
}
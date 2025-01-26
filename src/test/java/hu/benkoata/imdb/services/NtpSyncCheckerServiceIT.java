package hu.benkoata.imdb.services;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NtpSyncCheckerServiceIT {
    @Test
    void testSync() {
        assertNotNull(new NtpSyncCheckerService(LocalDateTime.now(), 1));
    }
    @Test
    void testOutOfSync() {
        LocalDateTime future = LocalDateTime.now().plusSeconds(10);
        assertThatThrownBy(() -> new NtpSyncCheckerService(future, 1)).isInstanceOf(IllegalStateException.class);
    }
}
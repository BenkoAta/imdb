package hu.benkoata.imdb.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
public class NtpSyncCheckerService {
    private static final String NTP_SERVER = "pool.ntp.org";

    public NtpSyncCheckerService(LocalDateTime referenceDateTime, long acceptableDeviationSecs) {
        try(NTPUDPClient ntpClient = new NTPUDPClient()) {
            InetAddress hostAddr = InetAddress.getByName(NTP_SERVER);
            TimeInfo timeInfo = ntpClient.getTime(hostAddr);
            LocalDateTime ntpDateTime = Instant.ofEpochMilli(timeInfo.getMessage().getTransmitTimeStamp().getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            log.debug("network   time: " + ntpDateTime);
            log.debug("reference time: " + referenceDateTime);
            long deviation = Math.abs(Duration.between(referenceDateTime, ntpDateTime).getSeconds());
            log.debug("deviation [s]: " + deviation);
            if (deviation > acceptableDeviationSecs) {
                throw new IllegalStateException(String.format("The time deviation from the network time is too large (%d sec).", deviation));
            }
        } catch (IOException ioe) {
            throw new IllegalStateException("IO error!", ioe);
        }
    }
}

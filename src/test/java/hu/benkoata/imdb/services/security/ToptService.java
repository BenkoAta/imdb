package hu.benkoata.imdb.services.security;

import lombok.Setter;
import org.apache.commons.codec.binary.Base32;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Setter
public class ToptService {
    private static final long TIME_STEP = 30;
    private LocalDateTime referenceDateTime;

    public int generateToptCode(String secretKey) {
        Mac mac = getMacWithHmacSha1(secretKey);
        byte[] timeBytes = ByteBuffer.allocate(Long.BYTES)
                .putLong(getTimeStepCounter(TIME_STEP))
                .array();
        return dynamicTruncateToGetOtp(mac.doFinal(timeBytes));
    }


    private long getTimeStepCounter(long timeStep) {
        LocalDateTime currentDateTime = referenceDateTime == null ? LocalDateTime.now() : referenceDateTime;
        long currentTimeSeconds = currentDateTime.atZone(ZoneId.systemDefault())
                .toInstant()
                .getEpochSecond();
        return currentTimeSeconds / timeStep;
    }

    private Mac getMacWithHmacSha1(String secretKey) {
        Base32 base32 = new Base32();
        byte[] keyBytes = base32.decode(secretKey);
        Mac result;
        try {
            result = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "HmacSHA1");
            result.init(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Can not get Mac instance!", e);
        }
        return result;
    }
    public int dynamicTruncateToGetOtp(byte[] hmacResult) {
        int offset = hmacResult[hmacResult.length - 1] & 0x0F;
        int binary = ((hmacResult[offset] & 0x7F) << 24)
                | ((hmacResult[offset + 1] & 0xFF) << 16)
                | ((hmacResult[offset + 2] & 0xFF) << 8)
                | (hmacResult[offset + 3] & 0xFF);
        return binary % 1000000;
    }
}

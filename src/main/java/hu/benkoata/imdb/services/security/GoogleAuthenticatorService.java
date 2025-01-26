package hu.benkoata.imdb.services.security;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import hu.benkoata.imdb.exceptions.TotpAuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleAuthenticatorService {
    private static final String ISSUER = "imdb";
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public String getKey() {
        return gAuth.createCredentials().getKey();
    }
    public String getQRUrl(String userName, String key) {
        return String.format("otpauth://totp/%s?secret=%s&issuer=%s",
                userName, key, ISSUER);
    }

    public void authenticate(String requestURI, String gAuthKey, int totpCode) {
        if (!gAuth.authorize(gAuthKey, totpCode)) {
            throw new TotpAuthenticationException(requestURI);
        }
    }
}

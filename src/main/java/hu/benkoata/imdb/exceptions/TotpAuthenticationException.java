package hu.benkoata.imdb.exceptions;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;
@SuppressWarnings("java:S110")
public class TotpAuthenticationException extends AbstractThrowableProblem {
    public TotpAuthenticationException(String requestURI) {
        super(URI.create(requestURI + "/invalid-totp-code"), "Invalid TOTP code", Status.UNAUTHORIZED, "Access denied!");
    }
}

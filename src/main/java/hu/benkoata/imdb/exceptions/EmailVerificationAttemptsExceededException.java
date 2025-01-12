package hu.benkoata.imdb.exceptions;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

@SuppressWarnings("java:S110")
public class EmailVerificationAttemptsExceededException extends AbstractThrowableProblem {
    public EmailVerificationAttemptsExceededException(String requestURI) {
        super(URI.create(requestURI + "/email-verification-attempts-exceeded"),
                "Too many email verification attemts",
                Status.FORBIDDEN,
                "Too many tries, can not unlock account!");
    }
}

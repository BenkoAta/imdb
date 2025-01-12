package hu.benkoata.imdb.exceptions;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

@SuppressWarnings("java:S110")
public class DeletionVerificationAttemptsExceededException extends AbstractThrowableProblem {
    public DeletionVerificationAttemptsExceededException(String requestURI) {
        super(URI.create(requestURI + "/deletion-verification-attempts-exceeded"),
                "Too many verification attemts",
                Status.FORBIDDEN,
                "Too many tries, can not delete account!");
    }
}

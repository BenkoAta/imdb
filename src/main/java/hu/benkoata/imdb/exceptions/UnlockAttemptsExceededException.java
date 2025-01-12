package hu.benkoata.imdb.exceptions;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

@SuppressWarnings("java:S110")
public class UnlockAttemptsExceededException extends AbstractThrowableProblem {
    public UnlockAttemptsExceededException(String requestURI) {
        super(URI.create(requestURI + "/unlock-attempts-exceeded"),
                "Too many unlock attemts",
                Status.FORBIDDEN,
                "Too many tries, can not unlock account!");
    }
}

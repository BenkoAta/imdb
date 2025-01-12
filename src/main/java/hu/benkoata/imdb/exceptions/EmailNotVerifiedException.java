package hu.benkoata.imdb.exceptions;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

@SuppressWarnings("java:S110")
public class EmailNotVerifiedException extends AbstractThrowableProblem {
    public EmailNotVerifiedException(String requestURI) {
        super(URI.create(requestURI + "/email-not-verified"),
                "Email not yet verified!",
                Status.FORBIDDEN,
                "Not possible before email validification!");
    }
}

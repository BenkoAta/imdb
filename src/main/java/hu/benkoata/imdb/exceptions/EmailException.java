package hu.benkoata.imdb.exceptions;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

@SuppressWarnings("java:S110")
public class EmailException extends AbstractThrowableProblem {
    public EmailException(String requestURI) {
        super(URI.create(requestURI + "/email-exception"),
                "Email exception",
                Status.INTERNAL_SERVER_ERROR,
                "Can not send email!");
    }
}

package hu.benkoata.imdb.exceptions;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

@SuppressWarnings("java:S110")
public class InvalidUserIdException extends AbstractThrowableProblem {
    public InvalidUserIdException(String requestURI) {
        super(URI.create(requestURI + "/invalid-user-id"),
                "Invalod user id",
                Status.FORBIDDEN,
                "It is not allowed to use any user ID other than your own!");
    }
}

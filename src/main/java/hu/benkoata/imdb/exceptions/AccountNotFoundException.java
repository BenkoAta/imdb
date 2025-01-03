package hu.benkoata.imdb.exceptions;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

@SuppressWarnings("java:S110")
public class AccountNotFoundException extends AbstractThrowableProblem {
    public AccountNotFoundException(String requestURI, String name) {
        super(URI.create(requestURI + "/account-not-found"), "Account not found", Status.NOT_FOUND, "Account not found: " + name + "!");
    }
}

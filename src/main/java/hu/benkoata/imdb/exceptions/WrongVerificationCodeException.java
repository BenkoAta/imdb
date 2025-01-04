package hu.benkoata.imdb.exceptions;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

@SuppressWarnings("java:S110")
public class WrongVerificationCodeException extends AbstractThrowableProblem {
    public WrongVerificationCodeException(String requestURI, int verificationCode) {
        super(URI.create(requestURI + "/wrong-verification-code"),
                "Wrong verification code",
                Status.BAD_REQUEST,
                "Wrong verification code: " + verificationCode + "!");
    }
}

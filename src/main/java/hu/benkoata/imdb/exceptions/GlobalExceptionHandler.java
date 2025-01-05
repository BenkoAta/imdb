package hu.benkoata.imdb.exceptions;

import hu.benkoata.imdb.ImdbApplication;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@SuppressWarnings("unused")
public class GlobalExceptionHandler {
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentialsExceptionException(BadCredentialsException exception, HttpServletRequest request) {
        return createProblemDetail(Status.UNAUTHORIZED,
                exception,
                request.getRequestURI() + "/bad-credentials",
                "Bad credentials!");
    }

    private ProblemDetail createProblemDetail(Status status, Exception exception, String type, String title) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatusCode.valueOf(status.getStatusCode()));
        problemDetail.setType(URI.create(type));
        problemDetail.setTitle(title);
        problemDetail.setDetail(exception.getMessage());
        return problemDetail;
    }


    @ExceptionHandler(AccountStatusException.class)
    public ProblemDetail handleAccountStatusException(AccountStatusException exception, HttpServletRequest request) {
        return createProblemDetail(Status.FORBIDDEN,
                exception,
                request.getRequestURI() + "/account-locked",
                "Account locked!");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException exception, HttpServletRequest request) {
        return createProblemDetail(Status.FORBIDDEN,
                exception,
                request.getRequestURI() + "/access-denied",
                "Access denied!");
    }

    @ExceptionHandler(SignatureException.class)
    public ProblemDetail handleSignatureException(SignatureException exception, HttpServletRequest request) {
        return createProblemDetail(Status.FORBIDDEN,
                exception,
                request.getRequestURI() + "/invalid-signature",
                "Invalid signature!");
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ProblemDetail handleExpiredJwtException(ExpiredJwtException exception, HttpServletRequest request) {
        return createProblemDetail(Status.FORBIDDEN,
                exception,
                request.getRequestURI() + "/jwt-expired",
                "Jwt expired!\"");
    }

    @ExceptionHandler(AbstractThrowableProblem.class)
    public ProblemDetail handleAbstractThrowableProblem(AbstractThrowableProblem atp) {
        log.debug(getStackTraceFromException(ImdbApplication.class.getPackageName(), atp));
        ProblemDetail errorDetail = ProblemDetail.forStatus(atp.getStatus().getStatusCode());
        errorDetail.setType(atp.getType());
        errorDetail.setTitle(atp.getTitle());
        errorDetail.setDetail(atp.getDetail());
        return errorDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception exception, HttpServletRequest request) {
        log.debug(getStackTraceFromException(ImdbApplication.class.getPackageName(), exception));
        return createProblemDetail(Status.INTERNAL_SERVER_ERROR,
                exception,
                request.getRequestURI() + "/internal-server-error",
                "Internal server error!");
    }

    private static final long STACK_TRACE_ENTRIES = 5;

    private String getStackTraceFromException(String packageName, Throwable e) {
        AtomicInteger counter = new AtomicInteger();
        return Arrays.stream(e.getStackTrace())
                .filter(stackTraceElement -> stackTraceElement.getClassName().startsWith(packageName))
                .map(st -> counter.incrementAndGet() > STACK_TRACE_ENTRIES ? "..." : st.toString())
                .limit(STACK_TRACE_ENTRIES + 1)
                .collect(Collectors.joining("\n"));
    }
}
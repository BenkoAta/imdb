package hu.benkoata.imdb.services;

import hu.benkoata.imdb.exceptions.DeletionVerificationAttemptsExceededException;
import hu.benkoata.imdb.exceptions.EmailVerificationAttemptsExceededException;
import hu.benkoata.imdb.exceptions.UnlockAttemptsExceededException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EndpointUsageLimiterService {
    private static final int UNLOCK_TRIES = 3;
    private static final int EMAIL_VERIFICATION_TRIES = 3;
    private static final int DELETION_VERIFICATION_TRIES = 3;
    private final Map<Long, Integer> unlockCalls = new HashMap<>();
    private final Map<Long, Integer> emailVerificationCalls = new HashMap<>();
    private final Map<Long, Integer> deleteVerificationCalls = new HashMap<>();
    public void checkUnlockLimit(String requestURI, long id) {
        Integer callCounter = unlockCalls.computeIfAbsent(id, integer -> 0);
        callCounter++;
        unlockCalls.put(id, callCounter);
        if (callCounter > UNLOCK_TRIES) {
            throw new UnlockAttemptsExceededException(requestURI);
        }
    }

    public void resetUnlockLimit(long id) {
        unlockCalls.remove(id);
    }

    public void checkRegistrationLimit(String remoteHost) {
        //Todo programozni
    }

    public void checkEmailVerificationLimit(String requestURI, long id) {
        Integer callCounter = emailVerificationCalls.computeIfAbsent(id, integer -> 0);
        callCounter++;
        emailVerificationCalls.put(id, callCounter);
        if (callCounter > EMAIL_VERIFICATION_TRIES) {
            throw new EmailVerificationAttemptsExceededException(requestURI);
        }
    }

    public void checkDeleteVerificationLimit(String requestURI, long id) {
        Integer callCounter = deleteVerificationCalls.computeIfAbsent(id, integer -> 0);
        callCounter++;
        deleteVerificationCalls.put(id, callCounter);
        if (callCounter > DELETION_VERIFICATION_TRIES) {
            throw new DeletionVerificationAttemptsExceededException(requestURI);
        }
    }
    public void resetDeleteVerficationLimit(long id) {
        deleteVerificationCalls.remove(id);
    }
}

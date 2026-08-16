package dev.applytrack.backend.identity.verification;

import dev.applytrack.backend.error.BusinessRuleViolationException;
import dev.applytrack.backend.error.ErrorCode;

public class TooManyVerificationRequestsException extends BusinessRuleViolationException {
    public TooManyVerificationRequestsException() {
        super(
                ErrorCode.TOO_MANY_VERIFICATION_REQUESTS,
                "Sie haben die maximale Anzahl an Anfragen erreicht. Bitte versuchen Sie es später erneut");
    }
}

package dev.applytrack.backend.identity.verification;

import dev.applytrack.backend.error.BusinessRuleViolationException;
import dev.applytrack.backend.error.ErrorCode;

public class VerificationTokenExpiredException extends BusinessRuleViolationException {
    public VerificationTokenExpiredException() {
        super(
                ErrorCode.VERIFICATION_TOKEN_EXPIRED,
                "Der Bestätigungslink ist abgelaufen. Bitte fordern Sie einen neuen an.");
    }
}

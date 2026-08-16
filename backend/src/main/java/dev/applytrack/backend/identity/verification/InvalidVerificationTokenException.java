package dev.applytrack.backend.identity.verification;

import dev.applytrack.backend.error.BusinessRuleViolationException;
import dev.applytrack.backend.error.ErrorCode;

public class InvalidVerificationTokenException extends BusinessRuleViolationException {
    public InvalidVerificationTokenException() {
        super(
                ErrorCode.INVALID_VERIFICATION_TOKEN,
                "Der Bestätigungslink ist ungültig. Bitte fordern Sie einen neuen an.");
    }
}

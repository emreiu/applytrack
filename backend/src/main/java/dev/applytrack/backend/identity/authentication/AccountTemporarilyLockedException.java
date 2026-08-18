package dev.applytrack.backend.identity.authentication;

import dev.applytrack.backend.error.ErrorCode;

import java.time.Duration;

public class AccountTemporarilyLockedException extends RuntimeException {

    private final ErrorCode errorCode = ErrorCode.ACCOUNT_TEMPORARILY_LOCKED;

    public AccountTemporarilyLockedException(Duration remaining) {
        super("Konto ist vorübergehend gesperrt. Bitte versuchen Sie es in %d Minuten erneut."
                      .formatted(ceilMinutes(remaining)));
    }

    private static long ceilMinutes(Duration duration) {
        return (duration.toSeconds() + 59) / 60;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
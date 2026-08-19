package dev.applytrack.backend.identity.authentication;

import dev.applytrack.backend.error.ErrorCode;

public class InvalidRefreshTokenException extends RuntimeException {

    private final ErrorCode errorCode = ErrorCode.INVALID_REFRESH_TOKEN;

    public InvalidRefreshTokenException() {
        super("Sitzung ist ungültig oder abgelaufen. Bitte erneut anmelden.");
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
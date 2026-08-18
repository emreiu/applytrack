package dev.applytrack.backend.identity.authentication;

import dev.applytrack.backend.error.ErrorCode;

public class InvalidCredentialsException extends RuntimeException {

    private final ErrorCode errorCode = ErrorCode.INVALID_CREDENTIALS;

    public InvalidCredentialsException() {
        super("E-Mail-Adresse oder Passwort ist falsch.");
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
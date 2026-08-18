package dev.applytrack.backend.identity.authentication;

import dev.applytrack.backend.error.ErrorCode;

public class EmailNotVerifiedException extends RuntimeException {

    private final ErrorCode errorCode = ErrorCode.EMAIL_NOT_VERIFIED;

    public EmailNotVerifiedException() {
        super("E-Mail-Adresse wurde noch nicht bestätigt.");
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
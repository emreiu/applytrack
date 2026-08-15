package dev.applytrack.backend.error;

public abstract class BusinessRuleViolationException extends RuntimeException {

    private final ErrorCode errorCode;

    protected BusinessRuleViolationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
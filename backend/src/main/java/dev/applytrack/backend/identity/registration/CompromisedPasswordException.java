package dev.applytrack.backend.identity.registration;

import dev.applytrack.backend.error.BusinessRuleViolationException;
import dev.applytrack.backend.error.ErrorCode;

public class CompromisedPasswordException extends BusinessRuleViolationException {

    public CompromisedPasswordException() {
        super(ErrorCode.PASSWORD_COMPROMISED,
                "Dieses Passwort wurde in bekannten Datenlecks gefunden. Bitte wählen Sie ein anderes Passwort.");
    }
}
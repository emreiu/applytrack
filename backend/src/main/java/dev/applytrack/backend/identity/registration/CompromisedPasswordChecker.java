package dev.applytrack.backend.identity.registration;

public interface CompromisedPasswordChecker {

    boolean isCompromised(String rawPassword);
}

package dev.applytrack.backend.identity.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "E-Mail-Adresse darf nicht leer sein")
        @Email(message = "E-Mail-Adresse ist ungültig")
        String email,

        @NotBlank(message = "Passwort darf nicht leer sein")
        String password
) {
}

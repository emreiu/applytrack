package dev.applytrack.backend.identity.verification;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationRequest(

        @NotBlank(message = "E-Mail-Adresse darf nicht leer sein")
        @Email(message = "E-Mail-Adresse ist ungültig")
        String email
) {
}

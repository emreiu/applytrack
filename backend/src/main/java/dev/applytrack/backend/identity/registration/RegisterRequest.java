package dev.applytrack.backend.identity.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "E-Mail-Adresse darf nicht leer sein")
        @Email(message = "E-Mail-Adresse ist ungültig")
        String email,

        @NotBlank(message = "Password darf nicht leer sein")
        @Size(min = 12, message = "Das Passwort muss mindestens 12 Zeichen lang sein")
        String password,

        @NotBlank(message = "Benutzername darf nicht leer sein")
        String displayName
) {
}

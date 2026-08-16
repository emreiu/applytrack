package dev.applytrack.backend.identity.verification;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(

        @NotBlank(message = "Token darf nicht leer sein")
        String token
) {
}

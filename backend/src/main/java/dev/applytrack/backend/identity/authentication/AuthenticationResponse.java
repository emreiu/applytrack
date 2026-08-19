package dev.applytrack.backend.identity.authentication;

import java.util.Set;
import java.util.UUID;

public record AuthenticationResponse(
        String accessToken,
        long expiresInSeconds,
        UUID userId,
        String displayName,
        Set<String> roles
) {
}
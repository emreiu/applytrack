package dev.applytrack.backend.identity.authentication;

import java.time.Duration;

public record AuthenticationResult(
        AuthenticationResponse response,
        String rawRefreshToken,
        Duration refreshTokenMaxAge
) {
}
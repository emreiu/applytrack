package dev.applytrack.backend.identity.authentication;

import java.time.Duration;

public record LoginResult(
        LoginResponse response,
        String rawRefreshToken,
        Duration refreshTokenMaxAge
) {
}
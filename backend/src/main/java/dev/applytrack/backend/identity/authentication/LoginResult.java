package dev.applytrack.backend.identity.authentication;

import java.time.OffsetDateTime;

public record LoginResult(
        LoginResponse response,
        String rawRefreshToken,
        OffsetDateTime refreshTokenExpiresAt
) {
}
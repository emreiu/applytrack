package dev.applytrack.backend.common.crypto;

import java.security.SecureRandom;
import java.util.Base64;

public final class SecureTokenGenerator {

    private static final int TOKEN_BYTE_LENGTH = 32;

    private SecureTokenGenerator() {
    }

    public static String generate() {
        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}

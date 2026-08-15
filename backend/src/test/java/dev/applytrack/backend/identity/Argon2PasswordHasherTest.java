package dev.applytrack.backend.identity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Argon2PasswordHasherTest {

    private final Argon2PasswordHasher passwordHasher = new Argon2PasswordHasher();

    @Test
    void hashedPasswordMatchesOriginalRawPassword() {
        String rawPassword = "SicheresPasswort123";

        String hash = passwordHasher.hash(rawPassword);

        assertThat(passwordHasher.matches(rawPassword, hash)).isTrue();
    }

    @Test
    void hashedPasswordDoesNotMatchDifferentRawPassword() {
        String hash = passwordHasher.hash("SicheresPasswort123");

        assertThat(passwordHasher.matches("EinAnderesPasswort456", hash)).isFalse();
    }
}
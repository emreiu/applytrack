package dev.applytrack.backend.identity;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class VerificationTokenTest {

    @Test
    void isExpiredReturnsTrueWhenNowIsAfterExpiry() {
        OffsetDateTime expiresAt = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        VerificationToken token = new VerificationToken(
                mock(User.class), VerificationTokenType.EMAIL_VERIFICATION, "hash", expiresAt);

        assertThat(token.isExpired(expiresAt.plusSeconds(1))).isTrue();
    }

    @Test
    void isExpiredReturnsFalseWhenNowIsBeforeExpiry() {
        OffsetDateTime expiresAt = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        VerificationToken token = new VerificationToken(
                mock(User.class), VerificationTokenType.EMAIL_VERIFICATION, "hash", expiresAt);

        assertThat(token.isExpired(expiresAt.minusSeconds(1))).isFalse();
    }
}
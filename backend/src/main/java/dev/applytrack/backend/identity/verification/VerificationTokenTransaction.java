package dev.applytrack.backend.identity.verification;

import dev.applytrack.backend.identity.User;
import dev.applytrack.backend.identity.VerificationToken;
import dev.applytrack.backend.identity.VerificationTokenRepository;
import dev.applytrack.backend.identity.VerificationTokenType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Component
class VerificationTokenTransaction {

    private final VerificationTokenRepository verificationTokenRepository;

    VerificationTokenTransaction(VerificationTokenRepository verificationTokenRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @Transactional
    void issue(User user, String tokenHash, OffsetDateTime now, OffsetDateTime expiresAt) {
        var oldTokens = verificationTokenRepository.findByUserAndTypeAndConsumedAtIsNull(
                user, VerificationTokenType.EMAIL_VERIFICATION);
        verificationTokenRepository.deleteAll(oldTokens);

        VerificationToken token = new VerificationToken(
                user, VerificationTokenType.EMAIL_VERIFICATION, tokenHash, expiresAt);
        verificationTokenRepository.saveAndFlush(token);
    }

    @Transactional
    void consume(VerificationToken token, User user, OffsetDateTime now) {
        token.consume(now);
        user.verifyEmail(now);
        verificationTokenRepository.saveAndFlush(token);
    }
}

package dev.applytrack.backend.identity.verification;

import dev.applytrack.backend.identity.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class VerificationTokenService {

    private static final int MAX_REQUESTS_PER_HOUR = 3;

    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;
    private final VerificationTokenTransaction verificationTokenTransaction;
    private final EmailSender emailSender;
    private final String frontendBaseUrl;
    private final Clock clock;

    public VerificationTokenService(
            VerificationTokenRepository verificationTokenRepository,
            UserRepository userRepository,
            VerificationTokenTransaction verificationTokenTransaction,
            EmailSender emailSender,
            @Value("${app.frontend-base-url}") String frontendBaseUrl,
            Clock clock) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.userRepository = userRepository;
        this.verificationTokenTransaction = verificationTokenTransaction;
        this.emailSender = emailSender;
        this.frontendBaseUrl = frontendBaseUrl;
        this.clock = clock;
    }

    public void issueToken(User user) {
        OffsetDateTime now = OffsetDateTime.now(clock);

        long recentRequests = verificationTokenRepository.countByUserAndTypeAndCreatedAtAfter(
                user, VerificationTokenType.EMAIL_VERIFICATION, now.minusHours(1));
        if (recentRequests >= MAX_REQUESTS_PER_HOUR) {
            throw new TooManyVerificationRequestsException();
        }

        String rawToken = generateRawToken();
        String tokenHash = hash(rawToken);
        OffsetDateTime expiresAt = now.plusHours(24);

        verificationTokenTransaction.issue(user, tokenHash, now, expiresAt);

        sendVerificationEmail(user, rawToken);
    }

    public void verifyToken(String rawToken) {
        String tokenHash = hash(rawToken);

        VerificationToken token = verificationTokenRepository
                .findByTokenHashAndType(tokenHash, VerificationTokenType.EMAIL_VERIFICATION)
                .orElseThrow(InvalidVerificationTokenException::new);

        if (token.isConsumed()) {
            throw new InvalidVerificationTokenException();
        }

        OffsetDateTime now = OffsetDateTime.now(clock);
        if (token.isExpired(now)) {
            throw new VerificationTokenExpiredException();
        }

        verificationTokenTransaction.consume(token, token.getUser(), now);
    }

    public void resendVerificationToken(String email) {
        String normalizedEmail = email.trim().toLowerCase();

        userRepository.findByEmail(normalizedEmail)
                      .filter(user -> user.getStatus() == UserStatus.PENDING_VERIFICATION)
                      .ifPresent(this::issueToken);
    }

    // ----------------------------------------------------
    // Helper Methods
    // ----------------------------------------------------

    private String generateRawToken() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private void sendVerificationEmail(User user, String rawToken) {
        String link = frontendBaseUrl + "/verify-email?token=" + rawToken;
        emailSender.send(
                user.getEmail(),
                "Bitte bestätigen Sie Ihre E-Mail-Adresse",
                "Bitte bestätigen Sie Ihre E-Email-Adresse, indem Sie auf folgenden Link klicken: " + link
        );
    }
}

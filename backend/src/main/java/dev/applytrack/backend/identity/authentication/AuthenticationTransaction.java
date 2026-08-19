package dev.applytrack.backend.identity.authentication;

import dev.applytrack.backend.identity.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class AuthenticationTransaction {

    private final LoginAttemptRepository loginAttemptRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public AuthenticationTransaction(
            LoginAttemptRepository loginAttemptRepository,
            RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void recordFailedAttempt(
            String emailAttempted, User user, String ipHash,
            String userAgent, OffsetDateTime now) {
        loginAttemptRepository.save(
                new LoginAttempt(emailAttempted, user, true, now, ipHash, userAgent));
    }

    @Transactional
    public void recordCredentialsValidWithoutSession(
            String emailAttempted, User user,
            String ipHash, String userAgent,
            OffsetDateTime now) {
        loginAttemptRepository.save(
                new LoginAttempt(emailAttempted, user, true, now, ipHash, userAgent));
    }

    @Transactional
    public void completeSuccessfulLogin(
            String emailAttempted, User user, String refreshTokenHash,
            OffsetDateTime now, OffsetDateTime refreshExpiresAt,
            String deviceLabel, String ipHash, String userAgent) {
        loginAttemptRepository.save(
                new LoginAttempt(emailAttempted, user, true, now, ipHash, userAgent));

        user.recordLogin(now);
        userRepository.save(user);

        RefreshToken refreshToken = new RefreshToken(
                user, refreshTokenHash, now, refreshExpiresAt, deviceLabel, ipHash);
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void rotateRefreshToken(
            RefreshToken oldToken, User user, String newTokenHash,
            OffsetDateTime now, OffsetDateTime newExpiresAt,
            String deviceLabel, String ipHash) {
        oldToken.revoke(now);
        refreshTokenRepository.save(oldToken);

        RefreshToken newToken = new RefreshToken(
                user, newTokenHash, now, newExpiresAt, deviceLabel, ipHash);
        refreshTokenRepository.save(newToken);
    }

    @Transactional
    public void revokeAllActiveTokens(User user, OffsetDateTime now) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserAndRevokedAtIsNull(user);
        activeTokens.forEach(token -> token.revoke(now));
        refreshTokenRepository.saveAll(activeTokens);
    }
}

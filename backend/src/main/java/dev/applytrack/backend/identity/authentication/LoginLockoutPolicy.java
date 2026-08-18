package dev.applytrack.backend.identity.authentication;

import dev.applytrack.backend.identity.LoginAttempt;
import dev.applytrack.backend.identity.LoginAttemptRepository;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class LoginLockoutPolicy {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCKOUT_WINDOW = Duration.ofMinutes(15);

    private final LoginAttemptRepository loginAttemptRepository;
    private final Clock clock;

    public LoginLockoutPolicy(LoginAttemptRepository loginAttemptRepository, Clock clock) {
        this.loginAttemptRepository = loginAttemptRepository;
        this.clock = clock;
    }

    public Optional<OffsetDateTime> lockedUntil(String emailAttempted) {
        OffsetDateTime now = OffsetDateTime.now(clock);

        OffsetDateTime since = loginAttemptRepository
                .findTopByEmailAttemptedAndSuccessfulTrueOrderByAttemptedAtDesc(emailAttempted)
                .map(LoginAttempt::getAttemptedAt)
                .orElse(OffsetDateTime.MIN);

        List<LoginAttempt> recentFailures = loginAttemptRepository
                .findTop5ByEmailAttemptedAndSuccessfulFalseAndAttemptedAtAfterOrderByAttemptedAtDesc(
                        emailAttempted, since);

        if (recentFailures.size() < MAX_FAILED_ATTEMPTS) {
            return Optional.empty();
        }

        OffsetDateTime oldestOfRelevantFailures = recentFailures.get(MAX_FAILED_ATTEMPTS - 1).getAttemptedAt();
        OffsetDateTime lockedUntil = oldestOfRelevantFailures.plus(LOCKOUT_WINDOW);

        return lockedUntil.isAfter(now) ? Optional.of(lockedUntil) : Optional.empty();
    }
}
package dev.applytrack.backend.identity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, UUID> {

    Optional<LoginAttempt> findTopByEmailAttemptedAndSuccessfulTrueOrderByAttemptedAtDesc(
            String emailAttempted);

    List<LoginAttempt> findTop5ByEmailAttemptedAndSuccessfulFalseAndAttemptedAtAfterOrderByAttemptedAtDesc(
            String emailAttempted, OffsetDateTime after);
}
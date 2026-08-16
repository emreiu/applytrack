package dev.applytrack.backend.identity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByTokenHashAndType(
            String tokenHash, VerificationTokenType type);

    List<VerificationToken> findByUserAndTypeAndConsumedAtIsNull(
            User user, VerificationTokenType type);

    long countByUserAndTypeAndCreatedAtAfter(
            User user, VerificationTokenType type, OffsetDateTime since);
}

package dev.applytrack.backend.identity;

import dev.applytrack.backend.common.audit.AuditableEntity;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_token")
public class VerificationToken extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationTokenType type;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "payload")
    private String payload;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "consumed_at")
    private OffsetDateTime consumedAt;

    protected VerificationToken() {
        // for JPA
    }

    public VerificationToken(
            User user, VerificationTokenType type, String tokenHash, OffsetDateTime expiresAt) {
        this.user = user;
        this.type = type;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    // ----------------------------------------------------
    // Methods
    // ----------------------------------------------------

    public boolean isExpired(OffsetDateTime now) {
        return now.isAfter(expiresAt);
    }

    public boolean isConsumed() {
        return consumedAt != null;
    }

    public void consume(OffsetDateTime now) {
        this.consumedAt = now;
    }

    // ----------------------------------------------------
    // Getter & Setter
    // ----------------------------------------------------

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public VerificationTokenType getType() {
        return type;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public String getPayload() {
        return payload;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getConsumedAt() {
        return consumedAt;
    }
}

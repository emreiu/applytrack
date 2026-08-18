package dev.applytrack.backend.identity;

import jakarta.persistence.*;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_token")
@EntityListeners(AuditingEntityListener.class)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "issued_at", nullable = false)
    private OffsetDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Column(name = "device_label")
    private String deviceLabel;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    @Column(name = "ip_hash")
    private String ipHash;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    protected RefreshToken() {
        // for JPA
    }

    public RefreshToken(User user, String tokenHash, OffsetDateTime issuedAt, OffsetDateTime expiresAt,
                        String deviceLabel, String ipHash) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.deviceLabel = deviceLabel;
        this.ipHash = ipHash;
    }

    // ----------------------------------------------------
    // Methods
    // ----------------------------------------------------

    public void revoke(OffsetDateTime now) {
        this.revokedAt = now;
    }

    public void markUsed(OffsetDateTime now) {
        this.lastUsedAt = now;
    }

    public boolean isExpired(OffsetDateTime now) {
        return expiresAt.isBefore(now);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    // ----------------------------------------------------
    // Getter
    // ----------------------------------------------------

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public OffsetDateTime getIssuedAt() {
        return issuedAt;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getRevokedAt() {
        return revokedAt;
    }

    public String getDeviceLabel() {
        return deviceLabel;
    }

    public OffsetDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public String getIpHash() {
        return ipHash;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }
}
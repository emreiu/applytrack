package dev.applytrack.backend.identity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "login_attempt")
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email_attempted", nullable = false)
    private String emailAttempted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "successful", nullable = false)
    private boolean successful;

    @Column(name = "attempted_at", nullable = false)
    private OffsetDateTime attemptedAt;

    @Column(name = "ip_hash")
    private String ipHash;

    @Column(name = "user_agent")
    private String userAgent;

    protected LoginAttempt() {
        // for JPA
    }

    public LoginAttempt(String emailAttempted, User user, boolean successful, OffsetDateTime attemptedAt,
                        String ipHash, String userAgent) {
        this.emailAttempted = emailAttempted;
        this.user = user;
        this.successful = successful;
        this.attemptedAt = attemptedAt;
        this.ipHash = ipHash;
        this.userAgent = userAgent;
    }

    // ----------------------------------------------------
    // Getter
    // ----------------------------------------------------

    public UUID getId() {
        return id;
    }

    public String getEmailAttempted() {
        return emailAttempted;
    }

    public User getUser() {
        return user;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public OffsetDateTime getAttemptedAt() {
        return attemptedAt;
    }

    public String getIpHash() {
        return ipHash;
    }

    public String getUserAgent() {
        return userAgent;
    }
}
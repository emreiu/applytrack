package dev.applytrack.backend.identity;

import dev.applytrack.backend.common.audit.AuditableEntity;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    @Column(name = "locale", nullable = false)
    private String locale = "de";

    @Column(name = "time_zone", nullable = false)
    private String timeZone = "UTC";

    @Column(name = "email_verified_at")
    private OffsetDateTime emailVerifiedAt;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "deletion_requested_at")
    private OffsetDateTime deletionRequestedAt;

    @Column(name = "ghosting_threshold_days", nullable = false)
    private int ghostingThresholdDays = 30;

    @Column(name = "notifications_enabled", nullable = false)
    private boolean notificationsEnabled = true;

    protected User() {
        // for JPA
    }

    public User(String email, String passwordHash, String displayName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.status = UserStatus.PENDING_VERIFICATION;
    }

    // ----------------------------------------------------
    // Methods
    // ----------------------------------------------------

    public void assignRole(Role role) {
        this.roles.add(role);
    }

    // ----------------------------------------------------
    // Getter & Setter
    // ----------------------------------------------------

    public UUID getId() {
        return id;
    }

    public Set<Role> getRoles() {
        return Set.copyOf(roles);
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public String getLocale() {
        return locale;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public OffsetDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public OffsetDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public OffsetDateTime getDeletionRequestedAt() {
        return deletionRequestedAt;
    }

    public int getGhostingThresholdDays() {
        return ghostingThresholdDays;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
}

package dev.applytrack.backend.identity;

import dev.applytrack.backend.common.audit.AuditableEntity;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "role", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Role extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    protected Role() {
        // for JPA
    }

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}

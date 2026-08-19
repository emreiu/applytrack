package dev.applytrack.backend.identity.authentication;

import dev.applytrack.backend.identity.PasswordHasher;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DummyPasswordHashProvider {

    private final PasswordHasher passwordHasher;
    private String dummyHash;

    public DummyPasswordHashProvider(PasswordHasher passwordHasher) {
        this.passwordHasher = passwordHasher;
    }

    @PostConstruct
    void init() {
        this.dummyHash = passwordHasher.hash("dummy-password-for-timing-safety");
    }

    public String getDummyHash() {
        return dummyHash;
    }
}

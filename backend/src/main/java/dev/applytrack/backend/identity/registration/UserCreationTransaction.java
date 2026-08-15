package dev.applytrack.backend.identity.registration;

import dev.applytrack.backend.identity.Role;
import dev.applytrack.backend.identity.RoleRepository;
import dev.applytrack.backend.identity.User;
import dev.applytrack.backend.identity.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class UserCreationTransaction {

    private static final String ROLE_USER = "ROLE_USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    UserCreationTransaction(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    void execute(String normalizedEmail, String passwordHash, String displayName) {
        User user = new User(normalizedEmail, passwordHash, displayName);

        Role userRole = roleRepository.findByName(ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("Role " + ROLE_USER + " not found"));
        user.assignRole(userRole);

        userRepository.saveAndFlush(user);
    }
}

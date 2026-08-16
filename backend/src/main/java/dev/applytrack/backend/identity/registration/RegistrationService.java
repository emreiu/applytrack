package dev.applytrack.backend.identity.registration;

import dev.applytrack.backend.identity.*;
import dev.applytrack.backend.identity.verification.VerificationTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    private final UserRepository userRepository;
    private final UserCreationTransaction userCreationTransaction;
    private final PasswordHasher passwordHasher;
    private final CompromisedPasswordChecker compromisedPasswordChecker;
    private final EmailSender emailSender;
    private final VerificationTokenService verificationTokenService;

    RegistrationService(UserRepository userRepository,
                        UserCreationTransaction userCreationTransaction,
                        PasswordHasher passwordHasher,
                        CompromisedPasswordChecker compromisedPasswordChecker,
                        EmailSender emailSender,
                        VerificationTokenService verificationTokenService) {
        this.userRepository = userRepository;
        this.userCreationTransaction = userCreationTransaction;
        this.passwordHasher = passwordHasher;
        this.compromisedPasswordChecker = compromisedPasswordChecker;
        this.emailSender = emailSender;
        this.verificationTokenService = verificationTokenService;
    }

    public void register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            notifyExistingAccount(normalizedEmail);
            return;
        }

        if (compromisedPasswordChecker.isCompromised(request.password())) {
            throw new CompromisedPasswordException();
        }

        String passwordHash = passwordHasher.hash(request.password());

        User user;
        try {
            user = userCreationTransaction.execute(normalizedEmail, passwordHash, request.displayName());
        } catch (DataIntegrityViolationException e) {
            log.info("Registration race condition for email, treating as duplicate");
            notifyExistingAccount(normalizedEmail);
            return;
        }

        issueVerificationToken(user);
    }

    // ----------------------------------------------------
    // Helper Methods
    // ----------------------------------------------------

    private void issueVerificationToken(User user) {
        try {
            verificationTokenService.issueToken(user);
        } catch (Exception e) {
            // best effort: User is already created and saved. An error shouldn't cancel the registration.
            // User can request the token via resend-endpoint
            log.warn("Failed to issue verification token for user {}", user.getId(), e);
        }
    }

    private void notifyExistingAccount(String normalizedEmail) {
        try {
            emailSender.send(
                    normalizedEmail,
                    "Registrierungsversuch bei Applytrack",
                    "Es wurde versucht, mit dieser E-Mail-Adresse ein neues Konto zu registrieren. "
                            + "Sie besitzen bereits ein Konto. Falls Sie Ihr Passwort vergessen haben, "
                            + "nutzen Sie die Passwort-vergessen-Funktion."
            );
        } catch (Exception e) {
            // best effort: registration should still be processed despite email problems
            log.warn("Failed to send duplicate-registration notice", e);
        }
    }
}

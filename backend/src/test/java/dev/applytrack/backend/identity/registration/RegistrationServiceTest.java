package dev.applytrack.backend.identity.registration;

import dev.applytrack.backend.identity.EmailSender;
import dev.applytrack.backend.identity.PasswordHasher;
import dev.applytrack.backend.identity.User;
import dev.applytrack.backend.identity.UserRepository;
import dev.applytrack.backend.identity.verification.VerificationTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCreationTransaction userCreationTransaction;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private CompromisedPasswordChecker compromisedPasswordChecker;

    @Mock
    private EmailSender emailSender;

    @Mock
    private VerificationTokenService verificationTokenService;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void registersNewUserWithHashedPasswordAndAssignsUserRole() {
        RegisterRequest request = new RegisterRequest(
                "test@example.com", "SicheresPasswort123", "Max Mustermann");
        User createdUser = mock(User.class);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(compromisedPasswordChecker.isCompromised(anyString())).thenReturn(false);
        when(passwordHasher.hash(anyString())).thenReturn("hashed-password");
        when(userCreationTransaction.execute(
                "test@example.com", "hashed-password", "Max Mustermann"))
                .thenReturn(createdUser);

        registrationService.register(request);

        verify(userCreationTransaction).execute(
                "test@example.com", "hashed-password", "Max Mustermann");
    }

    @Test
    void issuesVerificationTokenAfterSuccessfulRegistration() {
        RegisterRequest request = new RegisterRequest(
                "test@example.com", "SicheresPasswort123", "Max Mustermann");
        User createdUser = mock(User.class);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(compromisedPasswordChecker.isCompromised(anyString())).thenReturn(false);
        when(passwordHasher.hash(anyString())).thenReturn("hashed-password");
        when(userCreationTransaction.execute(anyString(), anyString(), anyString())).thenReturn(
                createdUser);

        registrationService.register(request);

        verify(verificationTokenService).issueToken(createdUser);
    }

    @Test
    void normalizesEmailBeforePassingToUserCreation() {
        RegisterRequest request = new RegisterRequest(
                "  teSt@exAmpLe.COM ", "SicheresPasswort123", "Max");
        User createdUser = mock(User.class);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(compromisedPasswordChecker.isCompromised(anyString())).thenReturn(false);
        when(passwordHasher.hash(anyString())).thenReturn("hashed-password");
        when(userCreationTransaction.execute(anyString(), anyString(), anyString()))
                .thenReturn(createdUser);

        registrationService.register(request);

        verify(userCreationTransaction).execute("test@example.com", "hashed-password", "Max");
    }

    @Test
    void notifiesRegisteredUserWhenEmailExists() {
        RegisterRequest request = new RegisterRequest(
                "test@example.com", "SicheresPasswort123", "Max");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        registrationService.register(request);

        verify(userCreationTransaction, never()).execute(any(), any(), any());
        verify(emailSender).send(eq("test@example.com"), anyString(), anyString());
        verify(verificationTokenService, never()).issueToken(any());
    }

    @Test
    void throwsCompromisedPasswordExceptionWhenPasswordIsCompromised() {
        RegisterRequest request = new RegisterRequest(
                "test@example.com", "SicheresPasswort123", "Max");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(compromisedPasswordChecker.isCompromised(anyString())).thenReturn(true);

        assertThrows(
                CompromisedPasswordException.class, () -> registrationService.register(request));
        verify(userCreationTransaction, never()).execute(any(), any(), any());
    }

    @Test
    void treatsRaceConditionAsDuplicateRegistration() {
        RegisterRequest request = new RegisterRequest(
                "test@example.com", "SicheresPasswort123", "Max");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(compromisedPasswordChecker.isCompromised(anyString())).thenReturn(false);
        when(passwordHasher.hash(anyString())).thenReturn("hashed-password");
        doThrow(DataIntegrityViolationException.class).when(userCreationTransaction)
                                                      .execute(any(), any(), any());

        registrationService.register(request);

        verify(emailSender).send(eq("test@example.com"), anyString(), anyString());
        verify(verificationTokenService, never()).issueToken(any());
    }

    @Test
    void doesNotFailRegistrationWhenNotificationEmailFails() {
        RegisterRequest request = new RegisterRequest(
                "test@example.com", "SicheresPasswort123", "Max");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        doThrow(new RuntimeException("SMTP down")).when(emailSender)
                                                  .send(anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> registrationService.register(request));
    }

    @Test
    void doesNotFailRegistrationWhenVerificationTokenIssuanceFails() {
        RegisterRequest request = new RegisterRequest(
                "test@example.com", "SicheresPasswort123", "Max");
        User createdUser = mock(User.class);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(compromisedPasswordChecker.isCompromised(anyString())).thenReturn(false);
        when(passwordHasher.hash(anyString())).thenReturn("hashed-password");
        when(userCreationTransaction.execute(anyString(), anyString(), anyString()))
                .thenReturn(createdUser);
        doThrow(new RuntimeException("SMTP down"))
                .when(verificationTokenService).issueToken(any());

        assertDoesNotThrow(() -> registrationService.register(request));
    }
}

package dev.applytrack.backend.identity.verification;

import dev.applytrack.backend.identity.EmailSender;
import dev.applytrack.backend.identity.User;
import dev.applytrack.backend.identity.UserRepository;
import dev.applytrack.backend.identity.UserStatus;
import dev.applytrack.backend.identity.VerificationToken;
import dev.applytrack.backend.identity.VerificationTokenRepository;
import dev.applytrack.backend.identity.VerificationTokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationTokenServiceTest {

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private VerificationTokenTransaction verificationTokenTransaction;

    @Mock
    private EmailSender emailSender;

    @Mock
    private UserRepository userRepository;

    private VerificationTokenService verificationTokenService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        verificationTokenService = new VerificationTokenService(
                verificationTokenRepository,
                userRepository,
                verificationTokenTransaction,
                emailSender,
                "http://localhost:4200",
                fixedClock);
    }

    @Test
    void issuesTokenAndSendsEmailWhenUnderRateLimit() {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("test@example.com");
        when(verificationTokenRepository.countByUserAndTypeAndCreatedAtAfter(
                eq(user), eq(VerificationTokenType.EMAIL_VERIFICATION), any())).thenReturn(0L);

        verificationTokenService.issueToken(user);

        verify(verificationTokenTransaction).issue(eq(user), anyString(), any(), any());
        verify(emailSender).send(eq("test@example.com"), anyString(), anyString());
    }

    @Test
    void throwsTooManyRequestsExceptionWhenRateLimitReached() {
        User user = mock(User.class);
        when(verificationTokenRepository.countByUserAndTypeAndCreatedAtAfter(
                eq(user), eq(VerificationTokenType.EMAIL_VERIFICATION), any())).thenReturn(3L);

        assertThrows(
                TooManyVerificationRequestsException.class,
                () -> verificationTokenService.issueToken(user));

        verify(verificationTokenTransaction, never()).issue(any(), any(), any(), any());
        verify(emailSender, never()).send(any(), any(), any());
    }

    @Test
    void consumesTokenWhenValid() {
        User user = mock(User.class);
        VerificationToken token = mock(VerificationToken.class);
        when(token.isConsumed()).thenReturn(false);
        when(token.isExpired(any())).thenReturn(false);
        when(token.getUser()).thenReturn(user);
        when(verificationTokenRepository.findByTokenHashAndType(
                anyString(), eq(VerificationTokenType.EMAIL_VERIFICATION)))
                .thenReturn(Optional.of(token));

        verificationTokenService.verifyToken("raw-token");

        verify(verificationTokenTransaction).consume(eq(token), eq(user), any());
    }

    @Test
    void throwsInvalidTokenExceptionWhenTokenNotFound() {
        when(verificationTokenRepository.findByTokenHashAndType(anyString(), any())).thenReturn(
                Optional.empty());

        assertThrows(
                InvalidVerificationTokenException.class,
                () -> verificationTokenService.verifyToken("raw-token"));

        verify(verificationTokenTransaction, never()).consume(any(), any(), any());
    }

    @Test
    void throwsInvalidTokenExceptionWhenTokenAlreadyConsumed() {
        VerificationToken token = mock(VerificationToken.class);
        when(token.isConsumed()).thenReturn(true);
        when(verificationTokenRepository.findByTokenHashAndType(anyString(), any())).thenReturn(
                Optional.of(token));

        assertThrows(
                InvalidVerificationTokenException.class,
                () -> verificationTokenService.verifyToken("raw-token"));

        verify(verificationTokenTransaction, never()).consume(any(), any(), any());
    }

    @Test
    void throwsExpiredExceptionWhenTokenExpired() {
        VerificationToken token = mock(VerificationToken.class);
        when(token.isConsumed()).thenReturn(false);
        when(token.isExpired(any())).thenReturn(true);
        when(verificationTokenRepository.findByTokenHashAndType(anyString(), any())).thenReturn(
                Optional.of(token));

        assertThrows(
                VerificationTokenExpiredException.class,
                () -> verificationTokenService.verifyToken("raw-token"));

        verify(verificationTokenTransaction, never()).consume(any(), any(), any());
    }

    @Test
    void resendsTokenWhenUserExistsAndPendingVerification() {
        User user = mock(User.class);
        when(user.getStatus()).thenReturn(UserStatus.PENDING_VERIFICATION);
        when(user.getEmail()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(verificationTokenRepository.countByUserAndTypeAndCreatedAtAfter(
                eq(user), eq(VerificationTokenType.EMAIL_VERIFICATION), any())).thenReturn(0L);

        verificationTokenService.resendVerificationToken("test@example.com");

        verify(verificationTokenTransaction).issue(eq(user), anyString(), any(), any());
    }

    @Test
    void doesNothingWhenResendRequestedForUnknownEmail() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertDoesNotThrow(
                () -> verificationTokenService.resendVerificationToken("unknown@example.com"));

        verify(verificationTokenTransaction, never()).issue(any(), any(), any(), any());
    }

    @Test
    void doesNothingWhenResendRequestedForAlreadyActiveUser() {
        User user = mock(User.class);
        when(user.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertDoesNotThrow(
                () -> verificationTokenService.resendVerificationToken("test@example.com"));

        verify(verificationTokenTransaction, never()).issue(any(), any(), any(), any());
    }
}
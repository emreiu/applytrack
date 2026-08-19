package dev.applytrack.backend.identity.authentication;

import dev.applytrack.backend.common.crypto.SecureTokenGenerator;
import dev.applytrack.backend.common.crypto.Sha256Hasher;
import dev.applytrack.backend.config.JwtProperties;
import dev.applytrack.backend.identity.*;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final DummyPasswordHashProvider dummyPasswordHashProvider;
    private final LoginLockoutPolicy loginLockoutPolicy;
    private final AuthenticationTransaction authenticationTransaction;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            DummyPasswordHashProvider dummyPasswordHashProvider,
            LoginLockoutPolicy loginLockoutPolicy,
            AuthenticationTransaction authenticationTransaction,
            RefreshTokenRepository refreshTokenRepository,
            JwtEncoder jwtEncoder,
            JwtProperties jwtProperties,
            Clock clock) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.dummyPasswordHashProvider = dummyPasswordHashProvider;
        this.loginLockoutPolicy = loginLockoutPolicy;
        this.authenticationTransaction = authenticationTransaction;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    public AuthenticationResult login(
            String rawEmail, String rawPassword, String ipAddress, String userAgent) {
        String email = rawEmail.trim().toLowerCase();
        String ipHash = ipAddress == null ? null : Sha256Hasher.hash(ipAddress);
        OffsetDateTime now = OffsetDateTime.now(clock);

        Optional<User> userOptional = userRepository.findByEmail(email);

        Optional<OffsetDateTime> lockedUntil = loginLockoutPolicy.lockedUntil(email);
        if (lockedUntil.isPresent()) {
            authenticationTransaction.recordFailedAttempt(
                    email, userOptional.orElse(null), ipHash, userAgent, now);
            throw new AccountTemporarilyLockedException(Duration.between(now, lockedUntil.get()));
        }

        String hashToCompare = userOptional.map(User::getPasswordHash)
                                           .orElse(dummyPasswordHashProvider.getDummyHash());
        boolean passwordMatches = passwordHasher.matches(rawPassword, hashToCompare);

        if (userOptional.isEmpty() || !passwordMatches) {
            authenticationTransaction.recordFailedAttempt(
                    email, userOptional.orElse(null), ipHash, userAgent, now);
            throw new InvalidCredentialsException();
        }

        User user = userOptional.get();

        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            authenticationTransaction.recordCredentialsValidWithoutSession(
                    email, user, ipHash, userAgent, now);
            throw new EmailNotVerifiedException();
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            authenticationTransaction.recordFailedAttempt(email, user, ipHash, userAgent, now);
            throw new InvalidCredentialsException();
        }

        String rawRefreshToken = SecureTokenGenerator.generate();
        String refreshTokenHash = Sha256Hasher.hash(rawRefreshToken);
        OffsetDateTime refreshExpiresAt = now.plus(jwtProperties.refreshTokenTtl());

        authenticationTransaction.completeSuccessfulLogin(
                email, user, refreshTokenHash, now, refreshExpiresAt, userAgent, ipHash, userAgent);

        return buildResult(user, now, rawRefreshToken);
    }

    public AuthenticationResult refresh(
            String rawRefreshToken, String ipAddress, String userAgent) {
        if (rawRefreshToken == null) {
            throw new InvalidRefreshTokenException();
        }

        String tokenHash = Sha256Hasher.hash(rawRefreshToken);
        RefreshToken existingToken = refreshTokenRepository.findByTokenHash(tokenHash)
                                                           .orElseThrow(
                                                                   InvalidRefreshTokenException::new);

        OffsetDateTime now = OffsetDateTime.now(clock);

        if (existingToken.isRevoked()) {
            authenticationTransaction.revokeAllActiveTokens(existingToken.getUser(), now);
            throw new InvalidRefreshTokenException();
        }

        if (existingToken.isExpired(now)) {
            throw new InvalidRefreshTokenException();
        }

        User user = existingToken.getUser();
        String ipHash = ipAddress == null ? null : Sha256Hasher.hash(ipAddress);

        String newRawRefreshToken = SecureTokenGenerator.generate();
        String newTokenHash = Sha256Hasher.hash(newRawRefreshToken);
        OffsetDateTime newExpiresAt = now.plus(jwtProperties.refreshTokenTtl());

        authenticationTransaction.rotateRefreshToken(
                existingToken, user, newTokenHash, now, newExpiresAt, userAgent, ipHash);

        return buildResult(user, now, newRawRefreshToken);
    }

    // ----------------------------------------------------
    // Helper Methods
    // ----------------------------------------------------

    private AuthenticationResult buildResult(
            User user, OffsetDateTime now, String rawRefreshToken) {
        String accessToken = createAccessToken(user, now);

        AuthenticationResponse response = new AuthenticationResponse(
                accessToken,
                jwtProperties.accessTokenTtl().toSeconds(),
                user.getId(),
                user.getDisplayName(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
        );

        return new AuthenticationResult(response, rawRefreshToken, jwtProperties.refreshTokenTtl());
    }

    private String createAccessToken(User user, OffsetDateTime now) {
        Instant issuedAt = now.toInstant();
        Instant expiresAt = issuedAt.plus(jwtProperties.accessTokenTtl());

        Set<String> roleNames = user.getRoles().stream()
                                    .map(Role::getName)
                                    .collect(Collectors.toSet());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                                          .subject(user.getId().toString())
                                          .issuedAt(issuedAt)
                                          .expiresAt(expiresAt)
                                          .claim("roles", roleNames)
                                          .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
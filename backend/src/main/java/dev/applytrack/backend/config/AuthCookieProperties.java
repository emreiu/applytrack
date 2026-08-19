package dev.applytrack.backend.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.auth.cookie")
public record AuthCookieProperties(
        @NotBlank String name,
        boolean secure,
        @NotBlank String sameSite) {
}
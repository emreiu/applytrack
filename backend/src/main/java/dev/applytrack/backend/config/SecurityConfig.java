package dev.applytrack.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

// TODO E2-S06: durch echte Autorisierungsregeln ersetzen (@PreAuthorize, Rollenprüfung).
// Aktuell bewusst alles offen, da Security nur für Argon2/SecurityAuditorAware gebraucht wird.
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // This api is stateless (JWT via Authorization Header), no need for CSRF protection
                // since it's not cookie/session based
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
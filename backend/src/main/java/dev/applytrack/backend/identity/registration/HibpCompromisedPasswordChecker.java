package dev.applytrack.backend.identity.registration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

@Component
public class HibpCompromisedPasswordChecker implements CompromisedPasswordChecker {

    private static final Logger log = LoggerFactory.getLogger(HibpCompromisedPasswordChecker.class);
    private static final String API_URL = "https://api.pwnedpasswords.com/range/";

    private final RestClient restClient;

    public HibpCompromisedPasswordChecker() {
        this.restClient = RestClient.builder()
                .baseUrl(API_URL)
                .requestFactory(clientRequestFactoryWithTimeout())
                .build();
    }

    @Override
    public boolean isCompromised(String rawPassword) {
        try {
            String sha1Hex = sha1Hex(rawPassword);
            String prefix = sha1Hex.substring(0, 5);
            String suffix = sha1Hex.substring(5);

            String response = restClient.get()
                    .uri(prefix)
                    .retrieve()
                    .body(String.class);

            return response != null && response.lines()
                    .anyMatch(line -> line.startsWith(suffix));
        } catch (Exception e) {
            log.warn("HIBP check failed, failing open (treating password as not compromised)", e);
            return false;
        }
    }

    private String sha1Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02X", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 not available", e);
        }
    }

    private ClientHttpRequestFactory clientRequestFactoryWithTimeout() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(3).toMillis());
        return factory;
    }
}

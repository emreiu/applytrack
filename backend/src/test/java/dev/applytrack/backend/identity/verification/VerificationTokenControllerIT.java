package dev.applytrack.backend.identity.verification;

import dev.applytrack.backend.identity.EmailSender;
import dev.applytrack.backend.identity.User;
import dev.applytrack.backend.identity.UserRepository;
import dev.applytrack.backend.identity.UserStatus;
import dev.applytrack.backend.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VerificationTokenControllerIT extends AbstractIntegrationTest {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("token=(\\S+)");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenService verificationTokenService;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private EmailSender emailSender;

    @Test
    void verifiesEmailWithValidToken() throws Exception {
        User user = new User("test@example.com", "irrelevant-hash", "Max");
        userRepository.saveAndFlush(user);

        verificationTokenService.issueToken(user);
        String rawToken = extractTokenFromSentEmail();

        mockMvc.perform(post("/api/v1/auth/verify-email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        new VerifyEmailRequest(rawToken))))
               .andExpect(status().isNoContent());

        var verifiedUser = userRepository.findByEmail("test@example.com");
        assertThat(verifiedUser).isPresent();
        assertThat(verifiedUser.get().getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(verifiedUser.get().getEmailVerifiedAt()).isNotNull();
    }

    @Test
    void returnsBadRequestForUnknownToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/verify-email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        new VerifyEmailRequest("unknown-token"))))
               .andExpect(status().isBadRequest());
    }

    @Test
    void resendsVerificationTokenForPendingUser() throws Exception {
        User user = new User("test@example.com", "irrelevant-hash", "Max");
        userRepository.saveAndFlush(user);

        mockMvc.perform(post("/api/v1/auth/resend-verification")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(
                                        new ResendVerificationRequest("test@example.com"))))
               .andExpect(status().isNoContent());

        verify(emailSender).send(anyString(), anyString(), anyString());
    }

    private String extractTokenFromSentEmail() {
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).send(anyString(), anyString(), bodyCaptor.capture());

        Matcher matcher = TOKEN_PATTERN.matcher(bodyCaptor.getValue());
        if (!matcher.find()) {
            throw new IllegalStateException("No token found in sent email body");
        }
        return matcher.group(1);
    }
}
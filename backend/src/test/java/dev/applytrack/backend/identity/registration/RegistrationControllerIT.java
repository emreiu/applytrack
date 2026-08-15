package dev.applytrack.backend.identity.registration;

import dev.applytrack.backend.identity.PasswordHasher;
import dev.applytrack.backend.identity.User;
import dev.applytrack.backend.identity.UserRepository;
import dev.applytrack.backend.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RegistrationControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JsonMapper jsonMapper;
    @Autowired
    private PasswordHasher passwordHasher;

    @Test
    void registersNewUserSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "test@example.com", "Zx9#Qrmtplk284vwnB", "Max Mustermann");

        mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request)))
               .andExpect(status().isNoContent());

        var savedUser = userRepository.findByEmail("test@example.com");

        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getPasswordHash()).isNotEqualTo("Zx9#Qrmtplk284vwnB");
        assertThat(savedUser.get().getRoles())
                .extracting("name")
                .contains("ROLE_USER");
    }

    @Test
    void returnNoContentWithoutCreatingDuplicateWhenEmailAlreadyExists() throws Exception {
        User existingUser = new User("test@example.com", "password-hash", "Max");
        userRepository.saveAndFlush(existingUser);

        RegisterRequest request = new RegisterRequest(
                "test@example.com", "Zx9#Qrmtplk484vwnB", "Tom");

        mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request)))
               .andExpect(status().isNoContent());

        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(userRepository.findByEmail("test@example.com"))
                .get()
                .extracting(User::getDisplayName)
                .isEqualTo("Max");
    }

    @Test
    void returnsBadRequestWithFieldErrorsForInvalidInput() throws Exception {
        RegisterRequest request = new RegisterRequest("", "zukurz", "");

        mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(request)))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
               .andExpect(jsonPath("$.fieldErrors").isArray())
               .andExpect(jsonPath("$.fieldErrors[*].field")
                                  .value(containsInAnyOrder("email", "password", "displayName")));

        assertThat(userRepository.count()).isEqualTo(0);
    }
}
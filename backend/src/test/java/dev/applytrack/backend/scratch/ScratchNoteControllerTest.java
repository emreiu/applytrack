package dev.applytrack.backend.scratch;

import dev.applytrack.backend.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ScratchNoteControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void createsAndRetrievesAScratchNote() throws Exception {
        ScratchNote toCreate = new ScratchNote("via controller");

        String response = mockMvc.perform(post("/scratch-notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(toCreate)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String id = jsonMapper.readTree(response).get("id").asString();

        mockMvc.perform(get("/scratch-notes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("via controller"));
    }
}

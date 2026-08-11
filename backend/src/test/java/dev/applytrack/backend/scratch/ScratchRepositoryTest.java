package dev.applytrack.backend.scratch;

import dev.applytrack.backend.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class ScratchRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ScratchNoteRepository repository;

    @Test
    void savesAndFindsAScratchNote() {
        ScratchNote saved = repository.save(new ScratchNote("test content"));

        var found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo("test content");
    }
}

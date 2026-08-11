package dev.applytrack.backend.scratch;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScratchNoteTest {

    @Test
    void hasContentIsFalseForBlankContent() {
        assertThat(new ScratchNote("   ").hasContent()).isFalse();
    }

    @Test
    void hasContentIsTrueForNonBlankContent() {
        assertThat(new ScratchNote("hello").hasContent()).isTrue();
    }
}

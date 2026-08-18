package dev.applytrack.backend.common.crypto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Sha256HasherTest {

    @Test
    void producesConsistentHashForSameInput() {
        assertThat(Sha256Hasher.hash("test")).isEqualTo(Sha256Hasher.hash("test"));
    }

    @Test
    void producesDifferentHashForDifferentInput() {
        assertThat(Sha256Hasher.hash("test")).isNotEqualTo(Sha256Hasher.hash("other"));
    }

    @Test
    void producesSixtyFourCharacterHexString() {
        assertThat(Sha256Hasher.hash("test")).hasSize(64);
    }
}
package dev.applytrack.backend.scratch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScratchNoteRepository extends JpaRepository<ScratchNote, UUID> {
}

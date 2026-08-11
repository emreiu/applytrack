package dev.applytrack.backend.scratch;

import dev.applytrack.backend.error.ResourceNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/scratch-notes")
public class ScratchNoteController {

    private final ScratchNoteRepository repository;

    ScratchNoteController(ScratchNoteRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    ScratchNote create(@RequestBody ScratchNote note) {
        return repository.save(note);
    }

    @GetMapping("/{id}")
    ScratchNote getById(@PathVariable UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ScratchNote.class, id));
    }
}

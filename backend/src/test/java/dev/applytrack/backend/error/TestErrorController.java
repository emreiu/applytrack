package dev.applytrack.backend.error;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/errors")
class TestErrorController {

    @PostMapping("/validation")
    public void validation(@Valid @RequestBody TestRequest request) {
    }

    @PostMapping("/not-found")
    public void notFound() {
        throw new ResourceNotFoundException(TestErrorController.class, "abc-123");
    }

    @PostMapping("/conflict")
    public void conflict() {
        throw new DataIntegrityViolationException("duplicate key");
    }

    @PostMapping("/unexpected")
    public void unexpected() {
        throw new IllegalStateException("boom");
    }

    record TestRequest(@NotBlank String name) {}
}
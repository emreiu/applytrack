package dev.applytrack.backend.identity.registration;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.base-path}/auth/register")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void register(@Valid @RequestBody RegisterRequest request) {
        registrationService.register(request);
    }
}

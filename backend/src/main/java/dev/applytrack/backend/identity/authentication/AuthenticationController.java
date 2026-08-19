package dev.applytrack.backend.identity.authentication;

import dev.applytrack.backend.config.AuthCookieProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.base-path}/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final AuthCookieProperties cookieProperties;
    private final String cookiePath;

    public AuthenticationController(
            AuthenticationService authenticationService,
            AuthCookieProperties cookieProperties,
            @Value("${api.base-path}") String apiBasePath) {
        this.authenticationService = authenticationService;
        this.cookieProperties = cookieProperties;
        this.cookiePath = apiBasePath + "/auth";
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        AuthenticationResult result = authenticationService.login(
                request.email(),
                request.password(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent"));

        ResponseCookie cookie = buildRefreshTokenCookie(result);

        return ResponseEntity.ok()
                             .header(HttpHeaders.SET_COOKIE, cookie.toString())
                             .body(result.response());
    }

    private ResponseCookie buildRefreshTokenCookie(AuthenticationResult result) {
        return ResponseCookie.from(cookieProperties.name(), result.rawRefreshToken())
                             .httpOnly(true)
                             .secure(cookieProperties.secure())
                             .sameSite(cookieProperties.sameSite())
                             .path(cookiePath)
                             .maxAge(result.refreshTokenMaxAge())
                             .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(
            @CookieValue(name = "${app.auth.cookie.name}", required = false) String refreshToken,
            HttpServletRequest httpRequest) {
        AuthenticationResult result = authenticationService.refresh(
                refreshToken, httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));

        ResponseCookie cookie = buildRefreshTokenCookie(result);

        return ResponseEntity.ok()
                             .header(HttpHeaders.SET_COOKIE, cookie.toString())
                             .body(result.response());
    }
}
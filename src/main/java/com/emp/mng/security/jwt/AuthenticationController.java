package com.emp.mng.security.jwt;

import com.emp.mng.security.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request){
        return authenticationService.login(request);
    }
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        return ResponseEntity.ok(
                authenticationService.refreshToken(request)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(
            org.springframework.security.core.Authentication authentication) {

        return ResponseEntity.ok(
                authenticationService.logout(authentication.getName())
        );
    }
}

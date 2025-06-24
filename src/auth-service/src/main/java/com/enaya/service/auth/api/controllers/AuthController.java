package com.enaya.service.auth.api.controllers;

import com.enaya.service.auth.api.dto.*;
import com.enaya.service.auth.application.AuthenticationService;
import com.enaya.service.auth.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) throws AuthenticationException {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) throws AuthenticationException {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PostMapping("/oauth2/login")
    public ResponseEntity<AuthResponse> oauth2Login(@Valid @RequestBody OAuth2AuthRequest request) throws AuthenticationException {
        return ResponseEntity.ok(authenticationService.oauth2Login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        authenticationService.logout(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) throws AuthenticationException {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }

    @PostMapping("/password/reset-request")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) throws AuthenticationException {
        authenticationService.requestPasswordReset(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetConfirmation request) throws AuthenticationException {
        authenticationService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify")
    public ResponseEntity<Boolean> verifyToken(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(authenticationService.verifyToken(token));
    }
}
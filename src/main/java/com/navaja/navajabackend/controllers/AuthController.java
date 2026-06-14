package com.navaja.navajabackend.controllers;

import com.navaja.navajabackend.dto.LoginRequest;
import com.navaja.navajabackend.dto.LoginResponse;
import com.navaja.navajabackend.dto.RegistroRequest;
import com.navaja.navajabackend.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegistroRequest request) {
        authService.registrar(request);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.iniciarSesion(request.email(), request.contrasena()));
    }
}


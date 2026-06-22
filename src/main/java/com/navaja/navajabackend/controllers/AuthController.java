package com.navaja.navajabackend.controllers;

import com.navaja.navajabackend.dto.LoginRequest;
import com.navaja.navajabackend.dto.LoginResponse;
import com.navaja.navajabackend.dto.RegistroRequest;
import com.navaja.navajabackend.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.navaja.navajabackend.dto.UserProfileDto;
import java.security.Principal;

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

    @GetMapping("/api/auth/me")
    public ResponseEntity<UserProfileDto> getMe(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(authService.obtenerPerfilUsuario(principal.getName()));
    }
}

